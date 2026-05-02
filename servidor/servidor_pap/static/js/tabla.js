const INTERVALO_MS = 5000;

let entradasCache = [];

function obtenerFiltros() {
    return {
        id:      document.getElementById('filtro-id').value.toLowerCase(),
        usuario: document.getElementById('filtro-usuario').value.toLowerCase(),
        fase:    document.getElementById('filtro-fase').value.toLowerCase(),
        fecha:   document.getElementById('filtro-fecha').value.toLowerCase(),
        entrada: document.getElementById('filtro-entrada').value.toLowerCase(),
        salida:  document.getElementById('filtro-salida').value.toLowerCase(),
    };
}

function renderizarTabla() {
    const campo = document.getElementById('campo-orden').value;
    const direccion = document.getElementById('direccion-orden').value;
    const filtros = obtenerFiltros();

    const entradas = [...entradasCache]
        .filter(e => {
            const fechaStr = new Date(e.fecha).toLocaleString().toLowerCase();
            return (
                String(e.id).toLowerCase().includes(filtros.id) &&
                e.nombre_usuario.toLowerCase().includes(filtros.usuario) &&
                e.nombre_fase.toLowerCase().includes(filtros.fase) &&
                fechaStr.includes(filtros.fecha) &&
                e.entrada.toLowerCase().includes(filtros.entrada) &&
                e.salida.toLowerCase().includes(filtros.salida)
            );
        })
        .sort((a, b) => {
        let va = a[campo];
        let vb = b[campo];
        if (campo === 'fecha') {
            va = new Date(va);
            vb = new Date(vb);
        } else {
            va = va.toLowerCase();
            vb = vb.toLowerCase();
        }
        if (va < vb) return direccion === 'asc' ? -1 : 1;
        if (va > vb) return direccion === 'asc' ? 1 : -1;
        return 0;
    });

    const tbody = document.getElementById('cuerpo-tabla');
    tbody.innerHTML = '';
    entradas.forEach(e => {
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
}

async function cargarEntradas() {
    try {
        const resp = await fetch('/api/obtener_entradas');
        const data = await resp.json();
        if (!resp.ok) {
            document.getElementById('mensaje-error').textContent = data.error;
            return;
        }
        document.getElementById('mensaje-error').textContent = '';
        entradasCache = data.entradas;
        renderizarTabla();
    } catch (err) {
        document.getElementById('mensaje-error').textContent = 'Error al cargar entradas: ' + err.message;
    }
}

document.getElementById('campo-orden').addEventListener('change', renderizarTabla);
document.getElementById('direccion-orden').addEventListener('change', renderizarTabla);

['filtro-id', 'filtro-usuario', 'filtro-fase', 'filtro-fecha', 'filtro-entrada', 'filtro-salida']
    .forEach(id => document.getElementById(id).addEventListener('input', renderizarTabla));

cargarEntradas();
setInterval(cargarEntradas, INTERVALO_MS);
