package com.grosshaeuser.olmp.ui.internal_ui.shared.inventory;

public interface Printing {

    String jsPrintExpression = """
                    return new Promise((resolve, reject) => {
                        const frame = document.createElement('iframe');
                        frame.setAttribute('aria-hidden', 'true');
                        frame.setAttribute('sandbox', 'allow-same-origin allow-modals');
                        frame.style.cssText = 'position:fixed;left:-10000px;top:0;width:210mm;height:297mm;border:0';
                        frame.onload = async () => {
                            try {
                                const content = frame.contentDocument;
                                await content.fonts.ready;
                                await Promise.all(Array.from(content.images, image => image.decode()));
                                frame.contentWindow.addEventListener('afterprint', () => frame.remove(), { once: true });
                                frame.contentWindow.focus();
                                frame.contentWindow.print();
                                resolve(true);
                            } catch (failure) {
                                frame.remove();
                                reject(String(failure));
                            }
                        };
                        frame.srcdoc = $0;
                        document.body.appendChild(frame);
                    });
                    """;
}
