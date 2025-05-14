function scrollToTop() {
    window.scrollTo({ top: 0, behavior: 'smooth' });
}


    document.addEventListener('DOMContentLoaded', () => {
        const fontAwesomeScript = document.createElement('script');
        fontAwesomeScript.src = 'https://kit.fontawesome.com/a076d05399.js';
        fontAwesomeScript.crossorigin = 'anonymous';
        fontAwesomeScript.onload = () => console.log('Font Awesome loaded');
        fontAwesomeScript.onerror = () => console.warn('Failed to load Font Awesome, icons may not display');
        document.head.appendChild(fontAwesomeScript);

        // Rest of your existing code...
    });