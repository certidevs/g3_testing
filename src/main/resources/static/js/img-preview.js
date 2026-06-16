(function () {
    const input = document.getElementById('imageFile');
    const preview = document.getElementById('preview');

    // MODO EDITAR: si ya hay imagen guardada, mostrarla al cargar
    if (preview.getAttribute('src')) {
        preview.style.display = 'block';
    }

    // AL ELEGIR ARCHIVO: previsualizar el nuevo (antes de subir)
    input.addEventListener('change', function () {
        const file = this.files[0];
        if (file) {
            preview.src = URL.createObjectURL(file);
            preview.style.display = 'block';
        }
    });
})();