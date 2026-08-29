/* ============================================================
   APP DE LA INTERFAZ WEB - Navegación en árbol + API /api/*
   ============================================================ */

'use strict';

/** Último estado recibido del servidor. */
let estado = { seleccionHecha: false, arr: {} };

/** Método de búsqueda de la hoja actualmente abierta ("LINEAL", "HASH MOD"...). */
let metodoActual = null;

/**
 * true cuando la hoja abierta es la "Búsqueda por transformación de
 * claves": ahí el método real lo decide la función hash elegida por el
 * usuario (HASH MOD, HASH CUADRADO, HASH TRUNCAMIENTO o HASH PLEGAMIENTO).
 */
let esTransformacion = false;

/**
 * Devuelve el método que se envía al servidor en la hoja abierta.
 * En transformación el método ES la función hash marcada; en las otras dos
 * búsquedas es el método fijo (LINEAL / BINARIA), que SIEMPRE inserta con
 * la función hash que elija el usuario.
 */
function metodoReal() {
    if (esTransformacion) {
        const marcada = document.querySelector('input[name="funcionHash"]:checked');
        return marcada ? marcada.value : null;
    }
    return metodoActual;
}

/** @return tipo técnico de la estructura que usa el método indicado. */
function estructuraDeMetodo(nombre) {
    return (estado.busquedas || [])
        .find((b) => b.nombre === nombre)?.estructura || '';
}

/** true si el método indicado es un árbol de residuos (ARBOL DIGITAL / POR RESIDUOS). */
function esVersionArbol(nombre) {
    return estructuraDeMetodo(nombre) === 'RESIDUOS DIGITAL';
}

/** Habilita el selector de función hash (al abrir una hoja se vuelve a preguntar). */
function habilitarSelectorFuncion() {
    document.querySelectorAll('input[name="funcionHash"]').forEach((radio) => {
        radio.disabled = false;
    });
}

/** Bloquea el selector de función hash (regla: inmodificable una vez aplicada). */
function deshabilitarSelectorFuncion() {
    document.querySelectorAll('input[name="funcionHash"]').forEach((radio) => {
        radio.disabled = true;
    });
}

/**
 * Presenta la función hash con la que el método activo ya inserta: al
 * reabrir la hoja del método vigente, el selector queda en esa función.
 */
function preseleccionarFuncionActiva() {
    if (!estado.seleccionHecha || !estado.funcion) {
        return;
    }
    const esperada = esTransformacion ? estado.metodo : metodoActual;
    if (estado.metodo !== esperada) {
        return;
    }
    const coincidente = document.querySelector(
        'input[name="funcionHash"][value="' + estado.funcion + '"]');
    if (coincidente) {
        coincidente.checked = true;
    }
}

/** Pila de navegación: {id, migas} para el botón Volver. */
const historial = [{ id: 'vista-inicio', migas: [] }];

/**
 * Etiquetas visibles de las soluciones de colisión: el backend conserva sus
 * identificadores (necesarios para la lógica), la interfaz los muestra con
 * nombres más claros para el usuario.
 */
const ETIQUETAS_SOLUCION = {
    'DOBLE DISPERSION': 'DOBLE FUNCION HASH',
    'ENCADENAMIENTO': 'LISTAS ENLAZADAS'
};

/** Devuelve la etiqueta visible de una solución de colisión. */
function etiquetaSolucion(nombre) {
    return ETIQUETAS_SOLUCION[nombre] || nombre;
}

/** Reemplaza en cualquier texto los identificadores por sus etiquetas. */
function traducirMensaje(texto) {
    if (!texto) {
        return texto;
    }
    let resultado = String(texto);
    Object.keys(ETIQUETAS_SOLUCION).forEach((id) => {
        resultado = resultado.split(id).join(ETIQUETAS_SOLUCION[id]);
    });
    return resultado;
}

/** Función genérica para llamar a la API y leer su JSON. */
async function llamarApi(ruta) {
    const respuesta = await fetch(ruta, { method: 'POST' });
    if (!respuesta.ok) {
        throw new Error('El servidor respondió ' + respuesta.status);
    }
    return respuesta.json();
}

/** Recarga el estado completo del servidor. */
async function recargarEstado() {
    estado = await llamarApi('/api/estado');
    pintarSoluciones();
    pintarAvisoRango();
    pintarOperaciones();
    pintarEstructura();
    return estado;
}

// ====================================================================
// NAVEGACIÓN EN ÁRBOL
// ====================================================================

/** Oculta todas las vistas y muestra solo la indicada, con sus migas. */
function mostrarVista(id, migas) {
    document.querySelectorAll('.vista').forEach((v) => { v.hidden = true; });
    document.getElementById(id).hidden = false;
    pintarMigas(migas);
}

/** Navega a una vista nueva guardando la anterior en el historial. */
function navegarA(id, migas) {
    historial.push({ id, migas });
    mostrarVista(id, migas);
}

/** Regresa a la vista anterior del historial. */
function volver() {
    if (historial.length > 1) {
        historial.pop();
    }
    const destino = historial[historial.length - 1];
    mostrarVista(destino.id, destino.migas);
}

/** Vuelve siempre a la pantalla de inicio dejando el historial limpio. */
function volverInicio() {
    historial.length = 0;
    historial.push({ id: 'vista-inicio', migas: [] });
    mostrarVista('vista-inicio', []);
}

/** Dibuja las migas de pan (ruta de navegación). */
function pintarMigas(migas) {
    const ruta = document.getElementById('migaRuta');
    ruta.innerHTML = '';
    migas.forEach((texto) => {
        const etiqueta = document.createElement('span');
        etiqueta.className = 'miga-paso';
        etiqueta.textContent = '  › ' + texto;
        ruta.appendChild(etiqueta);
    });
}

// ====================================================================
// APERTURA DE HOJAS (MÉTODOS DE BÚSQUEDA) Y PLACEHOLDERS
// ====================================================================

/**
 * Abre la vista de configuración de un método hoja.
 *
 * @param nombre nombre del método registrado ("LINEAL", "BINARIA",
 *               "HASH MOD", "HASH CUADRADO", "HASH PLEGAMIENTO",
 *               "HASH TRUNCAMIENTO").
 */
function abrirMetodo(nombre) {
    metodoActual = nombre;
    esTransformacion = false;
    habilitarSelectorFuncion();

    const esArbol = esVersionArbol(nombre);

    let migas;
    if (nombre === 'LINEAL' || nombre === 'BINARIA') {
        migas = ['Algoritmos de búsqueda', 'Búsquedas internas', nombre];
    } else if (esArbol) {
        migas = ['Algoritmos de búsqueda', 'Búsquedas internas',
            'Búsquedas por residuos', nombre];
    } else {
        migas = ['Algoritmos de búsqueda', 'Búsquedas internas',
            'Transformación de claves', nombre];
    }

    document.getElementById('tituloMetodo').textContent = nombre;
    document.getElementById('ayudaMetodo').textContent = '';
    document.getElementById('filaFuncion').hidden = esArbol;
    document.getElementById('soluciones').closest('.colision')
        .style.opacity = '1';
    document.getElementById('soluciones').disabled = esArbol;

    // Reinicia el área de operaciones hasta aplicar la configuración.
    document.getElementById('zonaOperaciones').hidden = true;
    document.getElementById('listaPasos').innerHTML = '';
    document.getElementById('mensaje').textContent =
        'Aplique la configuración para comenzar a operar.';
    document.getElementById('mensaje').classList.remove('error', 'exito');
    document.getElementById('estadoEstructura').textContent = '';
    document.getElementById('visualEstructura').innerHTML = '';

    preseleccionarFuncionActiva();
    navegarA('vista-metodo', migas);
    pintarSoluciones();
    pintarAvisoRango();
}

/**
 * Abre la hoja de "Búsqueda por transformación de claves": el método real
 * lo fija la función hash marcada (HASH MOD, HASH CUADRADO, HASH
 * TRUNCAMIENTO o HASH PLEGAMIENTO), así que aquí NO hay botones intermedios.
 */
function abrirTransformacion() {
    metodoActual = 'TRANSFORMACION';
    esTransformacion = true;
    habilitarSelectorFuncion();

    const migas = ['Algoritmos de búsqueda', 'Búsquedas internas',
        'Búsqueda por transformación de claves'];

    document.getElementById('tituloMetodo').textContent =
        'Búsqueda por transformación de claves';
    document.getElementById('ayudaMetodo').textContent =
        'La función hash elegida abajo define el método de búsqueda y decide '
        + 'DÓNDE se inserta cada clave. No puede cambiarse una vez aplicada '
        + 'la configuración.';
    document.getElementById('filaFuncion').hidden = false;

    document.getElementById('soluciones').closest('.colision')
        .style.opacity = '1';
    document.getElementById('soluciones').disabled = false;

    document.getElementById('zonaOperaciones').hidden = true;
    document.getElementById('listaPasos').innerHTML = '';
    document.getElementById('mensaje').textContent =
        'Aplique la configuración para comenzar a operar.';
    document.getElementById('mensaje').classList.remove('error', 'exito');
    document.getElementById('estadoEstructura').textContent = '';
    document.getElementById('visualEstructura').innerHTML = '';

    preseleccionarFuncionActiva();
    navegarA('vista-metodo', migas);
    pintarSoluciones();
    pintarAvisoRango();
}

/** Abre una vista placeholder: solo informa que no hay funcionalidad aún. */
function abrirPlaceholder(titulo, texto) {
    document.getElementById('tituloPlaceholder').textContent = titulo;
    document.getElementById('mensajePlaceholder').textContent = texto;
    navegarA('vista-placeholder', ['…']);
}

// ====================================================================
// APLICACIÓN DE LA CONFIGURACIÓN DEL MÉTODO HOJA
// ====================================================================

/**
 * Configura (dígitos + tamaño), elige la función hash para insertar y
 * selecciona el método activo con la solución de colisiones indicada;
 * luego revela el área de operaciones. La función hash queda FIJADA en
 * este momento (no puede cambiarse después).
 */
async function aplicarMetodo() {
    const digitos = document.getElementById('digitos').value.trim();
    const tamano = document.getElementById('tamano').value.trim();
    const solucion = document.getElementById('soluciones').value;
    const mantener = document.getElementById('mantener').checked ? '1' : '0';

    if (digitos === '' || tamano === '') {
        mostrarMensaje('Indique los dígitos y el tamaño de la estructura.', true);
        return;
    }
    // Los árboles de residuos NO piden función hash (sus claves se ubican
    // por bits, no por dirección calculada); el resto sí la exige.
    const esArbol = esVersionArbol(metodoActual);
    let funcion = null;
    if (!esArbol) {
        const marcada = document.querySelector('input[name="funcionHash"]:checked');
        funcion = marcada ? marcada.value : null;
    }

    // En transformación el método ES la función hash elegida; en LINEAL y
    // BINARIA la función decide cómo se insertan las claves.
    const metodoEnviar = esTransformacion ? funcion : metodoActual;
    if (metodoEnviar === null || (!esArbol && funcion === null)) {
        mostrarMensaje('Seleccione primero el método y la función hash.', true);
        return;
    }
    try {
        const ruta = '/api/configurar?digitos=' + digitos + '&tamano=' + tamano;
        const configurado = await llamarApi(ruta);
        if (!configurado.ok) {
            mostrarMensaje(configurado.mensaje, true);
            return;
        }
        let rutaSeleccionar = '/api/seleccionar?metodo='
            + encodeURIComponent(metodoEnviar)
            + '&solucion=' + encodeURIComponent(solucion)
            + '&mantener=' + mantener;
        if (funcion) {
            rutaSeleccionar += '&funcion=' + encodeURIComponent(funcion);
        }
        const seleccionado = await llamarApi(rutaSeleccionar);
        if (!seleccionado.ok) {
            mostrarMensaje(seleccionado.mensaje, true);
            return;
        }
        deshabilitarSelectorFuncion();
        document.getElementById('zonaOperaciones').hidden = false;
        document.getElementById('listaPasos').innerHTML = '';
        await recargarEstado();
        mostrarMensaje(traducirMensaje(seleccionado.mensaje), false);
    } catch (error) {
        mostrarMensaje('Error conectando con el servidor: ' + error.message, true);
    }
}

// ====================================================================
// OPERACIONES SOBRE CLAVES (CON ANIMACIÓN VISUAL)
// ====================================================================

/** true mientras una animación está en curso (bloquea botones). */
let animando = false;

/** Temporizador que retira la marca de "hallado" tras ~2 segundos. */
let temporizadorHallado = null;

/** Habilita o bloquea los botones de operación durante la animación. */
function bloquearBotones(bloquear) {
    ['btnInsertar', 'btnBuscar', 'btnEliminar', 'btnReiniciar',
        'btnCargarEstructura', 'btnExportarEstructura', 'btnAplicarCarga'
    ].forEach((id) => {
        document.getElementById(id).disabled = bloquear;
    });
}

/** Muestra el texto del paso actual (o limpia la barra con limpiar=true). */
function animarEstado(texto, limpiar) {
    const barra = document.getElementById('estadoAnimacion');
    barra.textContent = texto || '';
    barra.className = 'estado-animacion' + (limpiar ? '' : ' activo');
}

/**
 * Localiza la celda que contiene una clave dada (por valor o dentro de una
 * cubeta de desbordes), para poder animarla.
 *
 * @param clave valor numérico buscado.
 * @return elemento .celda o null si no está renderizado.
 */
function ubicarCelda(clave) {
    const objetivo = String(clave);
    const celdas = document.querySelectorAll('#visualEstructura .celda');
    for (const celda of celdas) {
        const valor = celda.querySelector('.valor');
        if (valor && valor.textContent.trim() === objetivo) {
            return celda;
        }
        const chips = celda.querySelectorAll('.chip');
        for (const chip of chips) {
            if (chip.textContent.trim() === objetivo) {
                return celda;
            }
        }
    }
    return null;
}

/**
 * Localiza la celda (y chip de desborde) de un paso de búsqueda según el
 * índice interno que registró la estrategia.
 *
 * @param paso paso de la búsqueda (con .indice y .claveComparada).
 * @return {celda, chip} o null si no hay celda renderizada para ese índice.
 */
function celdaDePaso(paso) {
    const celda = document.querySelector(
        '#visualEstructura .celda[data-indice="' + paso.indice + '"]');
    if (!celda) {
        return null;
    }
    let chip = null;
    if (paso.claveComparada !== -1) {
        Array.from(celda.querySelectorAll('.chip')).forEach((c) => {
            if (c.textContent.trim() === String(paso.claveComparada)) {
                chip = c;
            }
        });
    }
    return { celda, chip };
}

/** Agrega un paso al registro del proceso de búsqueda (log). */
function anadirPasoLog(paso) {
    const contenedor = document.getElementById('listaPasos');
    const div = document.createElement('div');
    div.className = 'paso';
    div.innerHTML = '<span class="numero">Paso ' + paso.numero
        + '</span><span class="detalle">' + escapeHtml(paso.descripcion)
        + '</span>';
    contenedor.appendChild(div);
    contenedor.scrollTop = contenedor.scrollHeight;
}

/** Agrega la línea final de resultado al log. */
function pintarResumenBusqueda(resultado) {
    const contenedor = document.getElementById('listaPasos');
    const div = document.createElement('div');
    div.className = 'paso ' + (resultado.encontrada ? 'encontrado' : 'error');
    div.innerHTML = '<span class="numero">Resultado</span><span class="detalle">'
        + escapeHtml(resultado.mensaje) + '</span>';
    contenedor.appendChild(div);
    contenedor.scrollTop = contenedor.scrollHeight;
}

/** Agrega una línea de texto al log (usada para insertar/eliminar). */
function anadirLogTexto(texto, esError) {
    const contenedor = document.getElementById('listaPasos');
    const div = document.createElement('div');
    div.className = 'paso ' + (esError ? 'error' : 'encontrado');
    div.innerHTML = '<span class="numero">' + (esError ? 'Error' : 'Hecho')
        + '</span><span class="detalle">' + escapeHtml(texto) + '</span>';
    contenedor.appendChild(div);
    contenedor.scrollTop = contenedor.scrollHeight;
}

/**
 * Reproduce la búsqueda animada paso a paso: una luz recorre la celda
 * comparada (barrido), las ya descartadas quedan tenues y cada paso se
 * registra en el log del panel derecho.
 *
 * @param pasos     pasos devueltos por /api/buscar.
 * @param encontrada true si la búsqueda tuvo éxito (para el remate verde).
 */
async function animarBusqueda(pasos, encontrada) {
    const pasosLog = document.getElementById('listaPasos');
    pasosLog.innerHTML = '';

    let previo = null;
    for (const paso of pasos) {
        if (previo) {
            previo.celda.classList.add('barrido');
            previo.celda.classList.remove('comparando');
            if (previo.chip) {
                previo.chip.classList.remove('comparando');
            }
        }
        animarEstado('Paso ' + paso.numero + ': ' + paso.descripcion);
        const objetivo = celdaDePaso(paso);
        if (objetivo) {
            objetivo.celda.classList.add('comparando');
            if (objetivo.chip) {
                objetivo.chip.classList.add('comparando');
            }
            objetivo.celda.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
            previo = objetivo;
        } else {
            previo = null;
        }
        anadirPasoLog(paso);
        await new Promise((resolver) => setTimeout(resolver, 520));
    }
    if (previo) {
        previo.celda.classList.remove('comparando');
        if (previo.chip) {
            previo.chip.classList.remove('comparando');
        }
    }
}

/** Animación de inserción: estampa simple de la clave (sin pasos). */
function animarInsercion(clave) {
    const celda = ubicarCelda(clave);
    if (!celda) {
        return;
    }
    celda.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    celda.classList.add('insertando');
    setTimeout(() => {
        celda.classList.remove('insertando');
    }, 850);
}

/**
 * Reproduce la inserción animada PASO A PASO cuando el backend la registró
 * (familias HASH): primero muestra DÓNDE se presentó la colisión (celda en
 * rojo pulsante, con la insignia "¡Colisión!"), luego cómo la resuelve la
 * solución elegida —sondeando posición tras posición (se marcan en ámbar y
 * el camino queda tenue) o encadenando en la cubeta (el chip se "enciende"
 * en verde)— y por último estampa la clave en su destino.
 *
 * @param pasos pasos devueltos por /api/insertar (.tipo/.indice/.clave).
 * @param clave valor numérico insertado (respaldo para celdas sin pasos).
 */
async function animarInsercionConPasos(pasos, clave) {
    if (!pasos || pasos.length === 0) {
        animarInsercion(clave);
        return;
    }
    const contenedor = document.getElementById('listaPasos');
    contenedor.innerHTML = '';

    const limpiar = [];
    for (const paso of pasos) {
        animarEstado('Paso ' + paso.numero + ': ' + paso.descripcion);
        const celda = document.querySelector(
            '#visualEstructura .celda[data-indice="' + paso.indice + '"]');
        if (celda) {
            celda.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
            const destino = claseDePasoInsercion(celda, paso);
            if (destino) {
                destino.elem.classList.add(destino.clase);
                limpiar.push(() => destino.elem.classList.remove(destino.clase));
            }
        }
        anadirPasoLog(paso);
        await new Promise((resolver) => setTimeout(resolver, 700));
        // El sondeo termina su pulso: la celda queda tenue (camino recorrido).
        if (celda && paso.tipo === 'sondeo') {
            celda.classList.remove('sondeo');
            celda.classList.add('barrido');
            limpiar.push(() => celda.classList.remove('barrido'));
        }
    }
    // La celda donde se presentó la colisión deja de pulsar (el estado final
    // de la estructura ya muestra el resultado real de la inserción).
    limpiar.forEach((limpiarClase) => limpiarClase());
    animarEstado('', true);
}

/** @return {elem, clase} que pide el paso, o null si no aplica marca. */
function claseDePasoInsercion(celda, paso) {
    if (paso.tipo === 'directa' || paso.tipo === 'exito') {
        return { elem: celda, clase: 'insertando' };
    }
    if (paso.tipo === 'colision') {
        return { elem: celda, clase: 'colision' };
    }
    if (paso.tipo === 'sondeo') {
        return { elem: celda, clase: 'sondeo' };
    }
    if (paso.tipo === 'desborde') {
        // Resalta el chip recién encadenado; si no está visible, marca la celda.
        let chip = null;
        Array.from(celda.querySelectorAll('.chip')).forEach((c) => {
            if (c.textContent.trim() === String(paso.clave)) {
                chip = c;
            }
        });
        return chip ? { elem: chip, clase: 'nuevo' } : { elem: celda, clase: 'insertando' };
    }
    return null;
}

/** Animación de eliminación: la celda se "disuelve" antes de re-dibujar. */
async function animarEliminacion(clave) {
    const celda = ubicarCelda(clave);
    if (!celda) {
        return;
    }
    celda.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    celda.classList.add('eliminando');
    await new Promise((resolver) => setTimeout(resolver, 620));
}

async function operarClave(operacion) {
    if (animando) {
        return;
    }
    const valor = document.getElementById('clave').value.trim();
    if (valor === '') {
        mostrarMensaje('Escriba una clave.', true);
        return;
    }
    if (parseInt(valor, 10) < parseInt(estado.rangoMinimo, 10)
            || parseInt(valor, 10) > parseInt(estado.rangoMaximo, 10)) {
        mostrarMensaje('La clave ' + valor + ' está fuera del rango válido '
            + estado.rangoMinimo + '..' + estado.rangoMaximo + '.', true);
        return;
    }

    animando = true;
    bloquearBotones(true);
    animarEstado('');
    try {
        const ruta = '/api/' + operacion + '?clave=' + encodeURIComponent(valor);
        const resultado = await llamarApi(ruta);

        if (operacion === 'buscar') {
            await recargarEstado();
            await animarBusqueda(resultado.pasos || [], resultado.encontrada);
            if (resultado.encontrada) {
                const celda = document.querySelector(
                    '#visualEstructura .celda[data-indice="' + resultado.indice + '"]');
                if (celda) {
                    celda.classList.remove('comparando', 'barrido');
                    celda.classList.add('hallado');
                    celda.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
                    // El hallado dura ~2 segundos y NO queda en loop: la clase
                    // se retira al terminar la animación (onda x2).
                    clearTimeout(temporizadorHallado);
                    temporizadorHallado = setTimeout(() => {
                        celda.classList.remove('hallado');
                    }, 2200);
                }
            }
            pintarResumenBusqueda(resultado);
            mostrarMensaje(traducirMensaje(resultado.mensaje), !resultado.encontrada);
        } else {
            document.getElementById('listaPasos').innerHTML = '';
            if (!resultado.ok) {
                const texto = traducirMensaje(resultado.mensaje);
                anadirLogTexto(texto, true);
                mostrarMensaje(texto, true);
                return;
            }
            if (operacion === 'eliminar') {
                await animarEliminacion(parseInt(valor, 10));
            }
            await recargarEstado();
            if (operacion === 'insertar') {
                await animarInsercionConPasos(resultado.pasos || [],
                    parseInt(valor, 10));
            }
            anadirLogTexto(traducirMensaje(resultado.mensaje), false);
            mostrarMensaje(traducirMensaje(resultado.mensaje), false);
        }
    } catch (error) {
        mostrarMensaje('Error conectando con el servidor: ' + error.message, true);
    } finally {
        animarEstado('', true);
        animando = false;
        bloquearBotones(false);
    }
}

// ====================================================================
// CARGA / EXPORTACIÓN DE ESTRUCTURAS
// ====================================================================

/**
 * Limpia TODO y empieza desde cero en cualquier búsqueda: vacía la
 * estructura, cancela el método activo y regresa a la pantalla de inicio
 * para reconfigurar dígitos, tamaño, función y solución sin restricciones.
 */
async function limpiarTodo() {
    if (animando) {
        return;
    }
    animando = true;
    bloquearBotones(true);
    clearTimeout(temporizadorHallado);
    try {
        await llamarApi('/api/reiniciar');
        metodoActual = null;
        esTransformacion = false;
        await recargarEstado();

        document.getElementById('zonaOperaciones').hidden = true;
        document.getElementById('listaPasos').innerHTML = '';
        document.getElementById('visualEstructura').innerHTML = '';
        document.getElementById('estadoEstructura').textContent = '';
        document.getElementById('estadoAnimacion').textContent = '';
        document.getElementById('mensaje').textContent = '';
        document.getElementById('mensaje').classList.remove('error', 'exito');
        habilitarSelectorFuncion();
        volverInicio();
    } catch (error) {
        mostrarMensaje('Error limpiando: ' + error.message, true);
    } finally {
        animando = false;
        bloquearBotones(false);
    }
}

async function exportarEstructura() {
    try {
        const resultado = await llamarApi('/api/exportar');
        if (!resultado.ok) {
            mostrarMensaje(traducirMensaje(resultado.mensaje), true);
            return;
        }
        const datos = {
            version: 1,
            digitos: resultado.digitos,
            tamano: resultado.tamano,
            metodo: resultado.metodo,
            solucion: resultado.solucion,
            claves: resultado.claves || []
        };
        const texto = JSON.stringify(datos, null, 2);
        document.getElementById('estructuraTexto').value = texto;
        document.getElementById('zonaEstructura').hidden = false;
        descargarArchivo('estructura.json', texto);
        mostrarMensaje('Estructura exportada con ' + datos.claves.length
            + ' clave(s): archivo "estructura.json" descargado, y el JSON '
            + 'quedó listo para copiar.', false);
    } catch (error) {
        mostrarMensaje('Error exportando: ' + error.message, true);
    }
}

/** Descarga un archivo de texto en el navegador sin librerías. */
function descargarArchivo(nombre, contenido) {
    const blob = new Blob([contenido], { type: 'application/json;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const enlace = document.createElement('a');
    enlace.href = url;
    enlace.download = nombre;
    document.body.appendChild(enlace);
    enlace.click();
    document.body.removeChild(enlace);
    URL.revokeObjectURL(url);
}

/** Muestra el área para pegar la estructura exportada. */
function mostrarZonaCarga() {
    document.getElementById('estructuraTexto').value = '';
    document.getElementById('zonaEstructura').hidden = false;
    document.getElementById('estructuraTexto').focus();
}

/** Cancela la carga sin tocar la estructura. */
function cancelarCarga() {
    document.getElementById('zonaEstructura').hidden = true;
    document.getElementById('estructuraTexto').value = '';
}

/**
 * Lee el JSON pegado, extrae sus claves y las carga en el método activo
 * reconstruyendo su estructura (permite usar la misma estructura en
 * diferentes tipos de búsqueda).
 */
async function aplicarCarga() {
    try {
        const texto = document.getElementById('estructuraTexto').value.trim();
        if (texto === '') {
            mostrarMensaje('Pegue primero el JSON exportado.', true);
            return;
        }
        const datos = JSON.parse(texto);
        const claves = datos.claves;
        if (!Array.isArray(claves)) {
            throw new Error('El archivo no contiene la lista "claves".');
        }
        const ruta = '/api/cargar?claves=' + encodeURIComponent(claves.join(','));
        const resultado = await llamarApi(ruta);
        if (resultado.ok) {
            document.getElementById('zonaEstructura').hidden = true;
            await recargarEstado();
            mostrarMensaje(traducirMensaje(resultado.mensaje), false);
        } else {
            mostrarMensaje(traducirMensaje(resultado.mensaje), true);
        }
    } catch (error) {
        mostrarMensaje('Datos inválidos: ' + error.message, true);
    }
}

// ====================================================================
// PINTADO DE AVISOS Y SOLUCIONES
// ====================================================================

function pintarSoluciones() {
    const select = document.getElementById('soluciones');
    const soluciones = estado.soluciones || [];
    select.innerHTML = '';
    soluciones.forEach((solucion) => {
        const opcion = document.createElement('option');
        opcion.value = solucion;
        opcion.textContent = etiquetaSolucion(solucion);
        if (estado.solucion === solucion) {
            opcion.selected = true;
        }
        select.appendChild(opcion);
    });
}

function pintarAvisoRango() {
    const aviso = document.getElementById('avisoRango');
    if (metodoActual === null) {
        aviso.textContent = '';
        return;
    }
    aviso.textContent = 'Claves de ' + estado.digitos + ' dígito(s): rango '
        + estado.rangoMinimo + '..' + estado.rangoMaximo
        + ' | Tamaño de la estructura: ' + estado.tamano + ' espacios.';
    aviso.classList.remove('error', 'exito');
}

function pintarOperaciones() {
    const aviso = document.getElementById('mensaje');
    if (estado.seleccionHecha) {
        aviso.textContent = 'Método activo: ' + estado.metodo
            + ' | Solución de colisión: ' + etiquetaSolucion(estado.solucion) + '.';
        aviso.classList.remove('error', 'exito');
    }
}

function mostrarMensaje(texto, esError) {
    const aviso = document.getElementById('mensaje');
    aviso.textContent = texto;
    aviso.classList.remove('error', 'exito');
    aviso.classList.add(esError ? 'error' : 'exito');
}

// ====================================================================
// DIBUJO DE LA ESTRUCTURA ACTIVA
// ====================================================================

function pintarEstructura() {
    const estadoVisual = document.getElementById('estadoEstructura');
    const visual = document.getElementById('visualEstructura');

    if (!estado.seleccionHecha || !estado.arr || Object.keys(estado.arr).length === 0) {
        estadoVisual.textContent = 'Aún no hay estructura activa. Aplique la configuración.';
        estadoVisual.classList.remove('error', 'exito');
        visual.innerHTML = '';
        return;
    }
    estadoVisual.textContent = 'Tipo: ' + estado.arr.tipo
        + ' | Ocupados: ' + estado.arr.cantidad
        + ' | Tamaño: ' + estado.arr.tamano + '.';
    estadoVisual.classList.remove('error', 'exito');

    if (estado.arr.tipo === 'RESIDUOS DIGITAL') {
        visual.innerHTML = '';
        if (estado.arr.raiz) {
            visual.appendChild(dibujarArbol(estado.arr.raiz));
        } else {
            visual.textContent = '(árbol sin nodos)';
        }
        return;
    }
    visual.innerHTML = '';
    visual.appendChild(dibujarArreglo(estado.arr));
}

function dibujarArreglo(arr) {
    const fila = document.createElement('div');
    fila.className = 'fila-celdas';

    if (arr.esHash) {
        // En tablas pequeñas se muestra la grilla COMPLETA de direcciones
        // (para poder animar colisiones y sondeos en cualquier posición).
        const completa = (arr.tamano || 0) <= 200;
        if (completa) {
            fila.classList.add('completa');
        }
        arr.posiciones.forEach((valor, indice) => {
            const direccion = indice + 1;
            const tieneClave = valor !== -1;
            const tieneDesbordes = arr.desbordes && arr.desbordes[direccion];
            if (!completa && !tieneClave && !tieneDesbordes) {
                return;
            }
            const celda = crearCelda(indice, valor, 'Dir ' + direccion);
            if (tieneDesbordes) {
                const lista = document.createElement('div');
                lista.className = 'desbordes';
                (arr.desbordes[direccion] || []).forEach((clave) => {
                    const chip = document.createElement('span');
                    chip.className = 'chip';
                    chip.textContent = clave;
                    lista.appendChild(chip);
                });
                celda.appendChild(lista);
            }
            fila.appendChild(celda);
        });
        if (fila.children.length === 0) {
            const vacia = document.createElement('div');
            vacia.textContent = '(estructura vacía)';
            fila.appendChild(vacia);
        }
        return fila;
    }

    // Secuencial / Ordenada: una celda por espacio ocupado (compactas).
    const ocupados = arr.posiciones.map((valor, indice) => ({ indice, valor }))
        .filter((celda) => celda.valor !== -1);
    ocupados.forEach((celda) => {
        fila.appendChild(crearCelda(celda.indice, celda.valor,
            'Pos ' + celda.indice));
    });
    if (ocupados.length === 0) {
        const vacia = document.createElement('div');
        vacia.textContent = '(estructura vacía)';
        fila.appendChild(vacia);
    }
    return fila;
}

function crearCelda(indice, valor, etiqueta) {
    const celda = document.createElement('div');
    celda.className = 'celda ' + (valor !== -1 ? 'ocupada' : 'vacia');
    celda.dataset.indice = indice;
    celda.innerHTML = '<span class="indice">' + etiqueta + '</span>'
        + '<span class="valor">' + (valor === -1 ? '—' : valor) + '</span>';
    return celda;
}

function dibujarArbol(nodo) {
    const caja = document.createElement('div');
    caja.className = 'arbol';

    const cajaNodo = document.createElement('div');
    cajaNodo.className = 'nodo ' + (nodo.vacio ? 'vacio' : '');
    cajaNodo.innerHTML = '<span class="clave">'
        + (nodo.vacio ? '(vacío)' : nodo.clave) + '</span>'
        + (nodo.binario ? '<br><span class="bin">' + escapeHtml(nodo.binario) + '</span>' : '');
    caja.appendChild(cajaNodo);

    const ramas = document.createElement('div');
    ramas.className = 'ramas';
    if (nodo.izq) {
        const ramaIzq = document.createElement('div');
        ramaIzq.className = 'rama';
        ramaIzq.innerHTML = '<span class="etiqueta">bit 0</span>';
        ramaIzq.appendChild(dibujarArbol(nodo.izq));
        ramas.appendChild(ramaIzq);
    }
    if (nodo.der) {
        const ramaDer = document.createElement('div');
        ramaDer.className = 'rama';
        ramaDer.innerHTML = '<span class="etiqueta">bit 1</span>';
        ramaDer.appendChild(dibujarArbol(nodo.der));
        ramas.appendChild(ramaDer);
    }
    if (nodo.izq || nodo.der) {
        caja.appendChild(ramas);
    }
    return caja;
}

// ====================================================================
// UTILIDADES
// ====================================================================

function escapeHtml(texto) {
    return String(texto).replace(/[&<>"']/g, (c) => {
        const mapa = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' };
        return mapa[c];
    });
}

// ====================================================================
// ARRANQUE Y EVENTOS
// ====================================================================

// --- Navegación principal ---
document.getElementById('btnMigaInicio').addEventListener('click', volverInicio);

document.getElementById('btnVerAlgoritmos').addEventListener('click', () => {
    navegarA('vista-algoritmos', ['Algoritmos de búsqueda']);
});
document.getElementById('btnVerGrafos').addEventListener('click', () => {
    abrirPlaceholder('Grafos',
        'La sección de Grafos aún no está implementada. '
        + 'Estará disponible en una próxima versión.');
});
document.getElementById('btnVerInternas').addEventListener('click', () => {
    navegarA('vista-internas', ['Algoritmos de búsqueda', 'Búsquedas internas']);
});
document.getElementById('btnVerExternas').addEventListener('click', () => {
    abrirPlaceholder('Búsquedas externas',
        'La sección de Búsquedas externas aún no está implementada. '
        + 'Estará disponible en una próxima versión.');
});
document.getElementById('btnVerIndices').addEventListener('click', () => {
    abrirPlaceholder('Índices',
        'La sección de Índices aún no está implementada. '
        + 'Estará disponible en una próxima versión.');
});
document.getElementById('btnVerTransformacion').addEventListener('click', () => {
    abrirTransformacion();
});
document.getElementById('btnVerResiduos').addEventListener('click', () => {
    navegarA('vista-residuos',
        ['Algoritmos de búsqueda', 'Búsquedas internas', 'Búsquedas por residuos']);
});
document.getElementById('btnVerResiduosMultiples').addEventListener('click', () => {
    abrirPlaceholder('Árbol de búsqueda por residuos múltiples',
        'Esta estructura aún no está implementada en el aplicativo.');
});

// --- Botones "Volver" estáticos ---
document.querySelectorAll('.btn-volver[data-volver]').forEach((boton) => {
    boton.addEventListener('click', volver);
});
document.getElementById('btnVolverMetodo').addEventListener('click', volver);
document.getElementById('btnVolverPlaceholder').addEventListener('click', volver);

// --- Hojas: métodos de búsqueda ---
document.querySelectorAll('[data-metodo]').forEach((boton) => {
    boton.addEventListener('click', () => abrirMetodo(boton.dataset.metodo));
});

// --- Configuración de la hoja ---
document.getElementById('btnIniciarMetodo').addEventListener('click', aplicarMetodo);

// --- Operaciones ---
document.getElementById('btnInsertar').addEventListener('click', () => operarClave('insertar'));
document.getElementById('btnBuscar').addEventListener('click', () => operarClave('buscar'));
document.getElementById('btnEliminar').addEventListener('click', () => operarClave('eliminar'));

// --- Cargar / exportar estructura ---
document.getElementById('btnCargarEstructura').addEventListener('click', mostrarZonaCarga);
document.getElementById('btnExportarEstructura').addEventListener('click', exportarEstructura);
document.getElementById('btnReiniciar').addEventListener('click', limpiarTodo);
document.getElementById('btnAplicarCarga').addEventListener('click', aplicarCarga);
document.getElementById('btnCancelarCarga').addEventListener('click', cancelarCarga);

recargarEstado().catch((error) => {
    const aviso = document.getElementById('mensaje');
    aviso.textContent = 'No se pudo contactar el servidor: ' + error.message;
    aviso.classList.add('error');
});