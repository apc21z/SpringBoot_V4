const output = document.getElementById('responseOutput');
const statusEl = document.getElementById('status');
const pagesList = document.getElementById('pagesList');

function setStatus(type, text) {
    statusEl.className = `status ${type}`;
    statusEl.textContent = text;
}

function renderResult(data) {
    output.textContent = JSON.stringify(data, null, 2);
}

function validateBookForm() {
    return document.getElementById('crudForm').reportValidity();
}

function addPage(page = {}) {
    const pageEntry = document.createElement('fieldset');
    pageEntry.className = 'page-entry';

    const legend = document.createElement('legend');
    legend.textContent = 'Página';

    const titleLabel = document.createElement('label');
    titleLabel.textContent = 'Título';
    const titleInput = document.createElement('input');
    titleInput.className = 'page-title';
    titleInput.type = 'text';
    titleInput.maxLength = 255;
    titleInput.required = true;
    titleInput.value = page.title ?? '';
    titleLabel.append(titleInput);

    const textLabel = document.createElement('label');
    textLabel.textContent = 'Texto';
    const textArea = document.createElement('textarea');
    textArea.className = 'page-text';
    textArea.required = true;
    textArea.value = page.text ?? '';
    textLabel.append(textArea);

    const removeButton = document.createElement('button');
    removeButton.className = 'danger';
    removeButton.type = 'button';
    removeButton.textContent = 'Quitar página';
    removeButton.addEventListener('click', () => pageEntry.remove());

    pageEntry.append(legend, titleLabel, textLabel, removeButton);
    pagesList.append(pageEntry);
}

function setPages(pages) {
    pagesList.replaceChildren();
    pages.forEach(addPage);
}

function getBookPayload() {
    return {
        title: document.getElementById('title').value,
        author: document.getElementById('author').value,
        isbn: document.getElementById('isbn').value,
        pages: [...pagesList.querySelectorAll('.page-entry')].map(pageEntry => ({
            title: pageEntry.querySelector('.page-title').value,
            text: pageEntry.querySelector('.page-text').value
        }))
    };
}

addPage({ title: 'Introducción', text: 'Texto de ejemplo.' });
document.getElementById('addPageBtn').addEventListener('click', () => addPage());

async function getAuthHeaders(method) {
    const headers = { 'Content-Type': 'application/json' };
    if (!['GET', 'HEAD', 'OPTIONS'].includes(method)) {
        const csrfResponse = await fetch('/login', { cache: 'no-store' });
        const loginHtml = await csrfResponse.text();
        const csrfInput = new DOMParser()
            .parseFromString(loginHtml, 'text/html')
            .querySelector('input[name="_csrf"]');
        if (!csrfResponse.ok || !csrfInput) {
            throw new Error('No se pudo obtener el token CSRF.');
        }
        headers['X-XSRF-TOKEN'] = csrfInput.value;
    }
    return headers;
}

async function apiRequest(url, method = 'GET', body = null) {
    const options = {
        method,
        headers: await getAuthHeaders(method)
    };

    if (body !== null) {
        options.body = JSON.stringify(body);
    }

    const response = await fetch(url, options);
    const text = await response.text();
    let data = null;
    try {
        data = text ? JSON.parse(text) : null;
    } catch (error) {
        data = text;
    }

    if (!response.ok) {
        throw new Error(JSON.stringify({ status: response.status, body: data }, null, 2));
    }
    return data;
}

document.getElementById('loginForm').addEventListener('submit', async (event) => {
    event.preventDefault();
    try {
        const email = document.getElementById('username').value.trim();
        const password = document.getElementById('password').value.trim();

        await apiRequest('/api/auth/login', 'POST', { email, password });
        setStatus('ok', 'Login correcto. La cookie JWT quedó activa en el navegador.');
        renderResult({ authenticated: true });
    } catch (error) {
        setStatus('error', 'Fallo al hacer login.');
        renderResult(error.message);
    }
});

document.getElementById('listBtn').addEventListener('click', async () => {
    try {
        const data = await apiRequest('/api/books');
        setStatus('ok', 'Listado de libros obtenido con éxito.');
        renderResult(data);
    } catch (error) {
        setStatus('error', 'No se pudo obtener la lista.');
        renderResult(error.message);
    }
});

document.getElementById('getBtn').addEventListener('click', async () => {
    const id = document.getElementById('bookId').value;
    if (!id) {
        setStatus('warn', 'Introduce un ID para obtener un libro.');
        return;
    }
    try {
        const data = await apiRequest(`/api/books/${id}`);
        document.getElementById('title').value = data.title;
        document.getElementById('author').value = data.author;
        document.getElementById('isbn').value = data.isbn;
        setPages(data.pages || []);
        setStatus('ok', `Libro ${id} obtenido correctamente.`);
        renderResult(data);
    } catch (error) {
        setStatus('error', `No se pudo obtener el libro ${id}.`);
        renderResult(error.message);
    }
});

document.getElementById('createBtn').addEventListener('click', async () => {
    if (!validateBookForm()) {
        return;
    }
    try {
        const payload = getBookPayload();
        const data = await apiRequest('/api/books', 'POST', payload);
        setStatus('ok', 'Libro creado correctamente.');
        document.getElementById('bookId').value = data.id;
        renderResult(data);
    } catch (error) {
        setStatus('error', 'No se pudo crear el libro.');
        renderResult(error.message);
    }
});

document.getElementById('updateBtn').addEventListener('click', async () => {
    const id = document.getElementById('bookId').value;
    if (!id) {
        setStatus('warn', 'Introduce un ID para actualizar.');
        return;
    }
    if (!validateBookForm()) {
        return;
    }
    try {
        const payload = getBookPayload();
        const data = await apiRequest(`/api/books/${id}`, 'PUT', payload);
        setStatus('ok', `Libro ${id} actualizado correctamente.`);
        renderResult(data);
    } catch (error) {
        setStatus('error', 'No se pudo actualizar el libro.');
        renderResult(error.message);
    }
});

document.getElementById('deleteBtn').addEventListener('click', async () => {
    const id = document.getElementById('bookId').value;
    if (!id) {
        setStatus('warn', 'Introduce un ID para borrar.');
        return;
    }
    try {
        await apiRequest(`/api/books/${id}`, 'DELETE');
        setStatus('ok', `Libro ${id} eliminado correctamente.`);
        renderResult({ deletedId: id, status: 'success' });
    } catch (error) {
        setStatus('error', `No se pudo eliminar el libro ${id}.`);
        renderResult(error.message);
    }
});