(() => {
    const jobId = (document.querySelector('meta[name="job-id"]')?.content || '').trim();
    if (!jobId) return;

    // Elements
    const statusBox   = document.getElementById('statusBox');
    const statusTitle = document.getElementById('statusTitle');
    const statusMsg   = document.getElementById('statusMsg');
    const timeMsEl    = document.getElementById('timeMs');
    const sizeEl      = document.getElementById('size');
    const errBox      = document.getElementById('error');
    const errMsgEl    = document.getElementById('errMsg');
    const gridSection = document.getElementById('gridSection');
    const gridEl      = document.getElementById('nonogram');
    const refreshBtn  = document.getElementById('refreshBtn');
    const pollBadge   = document.getElementById('pollBadge');

    // Server enum names (strict)
    const STATUS = { QUEUED:'QUEUED', RUNNING:'RUNNING', SUCCESS:'SUCCESS', FAIL:'FAIL' };
    const classes = ['status-queued','status-running','status-success','status-fail'];

    function setBoxClass(c) { classes.forEach(k => statusBox.classList.remove(k)); if (c) statusBox.classList.add(c); }
    function classFor(s) {
        switch (s) {
            case STATUS.QUEUED:  return 'status-queued';
            case STATUS.RUNNING: return 'status-running';
            case STATUS.SUCCESS: return 'status-success';
            case STATUS.FAIL:    return 'status-fail';
            default:             return '';
        }
    }

    function renderGrid(boolGrid) {
        const rows = Array.isArray(boolGrid) ? boolGrid.length : 0;
        const cols = rows > 0 ? (Array.isArray(boolGrid[0]) ? boolGrid[0].length : 0) : 0;
        if (rows === 0 || cols === 0) { gridSection.classList.add('hidden'); return; }

        gridEl.style.setProperty('--rows', rows);
        gridEl.style.setProperty('--cols', cols);

        gridEl.innerHTML = '';
        const frag = document.createDocumentFragment();
        for (let r = 0; r < rows; r++) {
            const row = boolGrid[r] || [];
            for (let c = 0; c < cols; c++) {
                const div = document.createElement('div');
                div.className = row[c] ? 'cell filled' : 'cell';
                frag.appendChild(div);
            }
        }
        gridEl.appendChild(frag);
        gridSection.classList.remove('hidden');
        console.info('grid rendered', { rows, cols, cells: rows * cols });
    }

    function updateUI(d) {
        const s = d.status;
        statusTitle.textContent = s;
        setBoxClass(classFor(s));

        sizeEl.textContent = (d.rows && d.cols) ? `${d.rows}×${d.cols}` : '—';
        timeMsEl.textContent = (d.solutionTimeMs ?? '—');

        console.debug("'" + d.solutionTimeMs +"'");

        if (s === STATUS.QUEUED) {
            statusMsg.textContent = 'Queued…';
        } else if (s === STATUS.RUNNING) {
            statusMsg.textContent = 'Crunching the nonogram…';
        } else if (s === STATUS.SUCCESS) {
            statusMsg.textContent = 'Solved!';
            renderGrid(d.resultGrid);
            stopPolling();
        } else if (s === STATUS.FAIL) {
            statusMsg.textContent = 'Job failed.';
            errMsgEl.textContent = d.errorMessage || 'Unknown error';
            errBox.classList.remove('hidden');
            stopPolling();
        } else {
            statusMsg.textContent = 'In progress…';
        }
    }

    let timer = null;
    const POLL_MS = 1200;

    async function fetchStatus() {
        try {
            const url = `/api/jobs/${encodeURIComponent(jobId)}`;
            const t0 = performance.now();
            const resp = await fetch(url, { headers: { 'Accept': 'application/json' }});
            if (!resp.ok) throw new Error(`HTTP ${resp.status}`);
            const json = await resp.json();
            console.debug('poll', { jobId, status: json.status, ms: Math.round(performance.now()-t0) });
            updateUI(json);
        } catch (e) {
            console.warn('poll error', e.message);
        }
    }

    function startPolling() {
        if (timer) return;
        pollBadge.textContent = 'auto-updating…';
        fetchStatus(); // immediate
        timer = setInterval(fetchStatus, POLL_MS);
    }

    function stopPolling() {
        if (!timer) return;
        clearInterval(timer);
        timer = null;
        pollBadge.textContent = 'final';
        console.info('polling stopped');
    }

    refreshBtn?.addEventListener('click', fetchStatus);
    startPolling();
})();
