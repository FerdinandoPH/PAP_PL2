const INTERVALO_MS = 5000;

async function cargarEntradas() {
    try {
        const resp = await fetch('/api/obtener_entradas');
        const data = await resp.json();
        if (!resp.ok) {
            document.getElementById('mensaje-error').textContent = data.error;
            return;
        }
        document.getElementById('mensaje-error').textContent = '';
        const tbody = document.getElementById('cuerpo-tabla');
        tbody.innerHTML = '';
        data.entradas.forEach(e => {
            const tr = document.createElement('tr');
            const campos = [
                e.id,
                e.nombre_usuario,
                e.nombre_fase,
                new Date(e.fecha).toLocaleString(),
            ];
            campos.forEach(valor => {
                const td = document.createElement('td');
                td.textContent = valor;
                tr.appendChild(td);
            });
            const campos_io = ['entrada', 'salida'];
            campos_io.forEach(campo => {
                const td = document.createElement('td');
                const pre = document.createElement('pre');
                pre.className = 'celda-io';
                pre.textContent = e[campo];
                td.appendChild(pre);
                tr.appendChild(td);
            });
            tbody.appendChild(tr);

        });
    } catch (err) {
        document.getElementById('mensaje-error').textContent = 'Error al cargar entradas: ' + err.message;
    }
}

cargarEntradas();
setInterval(cargarEntradas, INTERVALO_MS);
