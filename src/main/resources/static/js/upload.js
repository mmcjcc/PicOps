// Drag-and-drop bulk upload. Progressive enhancement over the plain
// multipart form: files upload one at a time so each gets its own
// accepted/rejected status, then the page reloads to show the grid.
(function () {
    const zone = document.getElementById('dropzone');
    if (!zone) return;
    const albumId = zone.dataset.album;
    const csrf = document.querySelector('meta[name="_csrf"]').content;
    const input = document.getElementById('filepick');
    const list = document.getElementById('uplist');
    let busy = false;

    ['dragenter', 'dragover'].forEach(function (name) {
        zone.addEventListener(name, function (ev) {
            ev.preventDefault();
            zone.classList.add('drag');
        });
    });
    ['dragleave', 'drop'].forEach(function (name) {
        zone.addEventListener(name, function (ev) {
            ev.preventDefault();
            zone.classList.remove('drag');
        });
    });
    zone.addEventListener('drop', function (ev) {
        handle(ev.dataTransfer.files);
    });
    zone.addEventListener('click', function (ev) {
        if (ev.target === input) return;
        input.click();
    });
    input.addEventListener('change', function () {
        handle(input.files);
    });

    async function handle(files) {
        if (busy || !files.length) return;
        busy = true;
        let ok = 0;
        for (const file of files) {
            const li = document.createElement('li');
            li.textContent = file.name + ' — uploading…';
            list.appendChild(li);
            const form = new FormData();
            form.append('files', file);
            form.append('_csrf', csrf);
            try {
                const res = await fetch('/albums/' + albumId + '/pictures', {
                    method: 'POST',
                    body: form
                });
                const rejected = res.url.indexOf('uploaderror') !== -1;
                li.textContent = file.name + (rejected ? ' — rejected' : ' — done');
                li.className = rejected ? 'up-bad' : 'up-ok';
                if (!rejected) ok++;
            } catch (e) {
                li.textContent = file.name + ' — network error';
                li.className = 'up-bad';
            }
        }
        busy = false;
        if (ok > 0) {
            setTimeout(function () { location.reload(); }, 900);
        }
    }
})();
