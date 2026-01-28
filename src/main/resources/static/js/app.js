document.addEventListener('click', function (event) {
    const sizeLink = event.target.closest('.size-options a');
    if (sizeLink) {
        return;
    }

    const sizeDisabled = event.target.closest('.size-disabled');
    if (sizeDisabled) {
        const sizeName = sizeDisabled.dataset.size || '';
        alert('Size ' + sizeName + ' đã hết hàng.');
        event.preventDefault();
        return;
    }

    const cartButton = event.target.closest('.cart-quick-btn');
    if (cartButton) {
        event.preventDefault();
        return;
    }

    const card = event.target.closest('.product-card');
    if (card && card.dataset.detailUrl) {
        window.location.href = card.dataset.detailUrl;
    }
});

document.addEventListener('DOMContentLoaded', function () {
    const sizeContainer = document.querySelector('.size-options-detail');
    if (!sizeContainer) {
        return;
    }
    const productId = sizeContainer.dataset.productId;
    const messageBox = document.getElementById('detailMessage');
    const qtyInput = document.getElementById('detailQuantity');
    const addBtn = document.getElementById('detailAddToCart');
    const minusBtn = document.querySelector('.qty-btn[data-action="minus"]');
    const plusBtn = document.querySelector('.qty-btn[data-action="plus"]');
    let selected = null;

    function showMessage(text, isError) {
        if (!messageBox) {
            return;
        }
        messageBox.textContent = text;
        messageBox.classList.toggle('status-error', !!isError);
    }

    function clampQuantity(value, max) {
        let qty = parseInt(value || '1', 10);
        if (Number.isNaN(qty) || qty <= 0) {
            qty = 1;
        }
        if (max && qty > max) {
            qty = max;
        }
        return qty;
    }

    sizeContainer.addEventListener('click', function (event) {
        const button = event.target.closest('.size-option');
        if (!button) {
            return;
        }
        sizeContainer.querySelectorAll('.size-option').forEach(item => item.classList.remove('active'));
        button.classList.add('active');
        selected = {
            id: button.dataset.sizeId,
            name: button.dataset.sizeName,
            max: parseInt(button.dataset.max || '0', 10)
        };
        const nextQty = clampQuantity(qtyInput.value, selected.max);
        qtyInput.value = nextQty;
        showMessage('Đã chọn size ' + selected.name + '.', false);
    });

    function adjustQuantity(delta) {
        const max = selected ? selected.max : null;
        const next = clampQuantity((parseInt(qtyInput.value || '1', 10) || 1) + delta, max);
        qtyInput.value = next;
    }

    minusBtn && minusBtn.addEventListener('click', function () {
        adjustQuantity(-1);
    });

    plusBtn && plusBtn.addEventListener('click', function () {
        adjustQuantity(1);
    });

    qtyInput && qtyInput.addEventListener('input', function () {
        const max = selected ? selected.max : null;
        qtyInput.value = clampQuantity(qtyInput.value, max);
    });

    addBtn && addBtn.addEventListener('click', function () {
        if (!selected) {
            showMessage('Vui lòng chọn size trước khi thêm vào giỏ hàng.', true);
            return;
        }
        const quantity = clampQuantity(qtyInput.value, selected.max);
        if (selected.max && quantity > selected.max) {
            showMessage('Số lượng vượt quá tồn kho của size đã chọn.', true);
            return;
        }
        fetch('/cart/add', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: new URLSearchParams({
                productId: productId,
                sizeId: selected.id,
                quantity: quantity
            })
        })
            .then(async (response) => {
                const data = await response.json().catch(() => ({}));
                if (!response.ok) {
                    throw new Error(data.message || 'Không thể thêm vào giỏ hàng.');
                }
                showMessage(data.message || 'Đã thêm vào giỏ hàng.', false);
            })
            .catch((error) => {
                showMessage(error.message || 'Không thể thêm vào giỏ hàng.', true);
            });
    });
});

document.addEventListener('DOMContentLoaded', function () {
    function normalizePath(path) {
        return (path || '/').replace(/\/+$/, '') || '/';
    }

    function markActiveLinks(selector, options) {
        const mode = options && options.mode ? options.mode : 'exact';
        const allowAdminAlias = options && options.allowAdminAlias;
        const currentPath = normalizePath(window.location.pathname);
        const links = document.querySelectorAll(selector);

        links.forEach(link => {
            const href = link.getAttribute('href');
            if (!href || href.startsWith('#')) {
                return;
            }
            const linkPath = normalizePath(new URL(href, window.location.origin).pathname);
            let isActive = false;

            if (mode === 'prefix') {
                isActive = currentPath === linkPath || (linkPath !== '/' && currentPath.startsWith(linkPath));
            } else {
                isActive = currentPath === linkPath;
            }

            if (!isActive && allowAdminAlias && currentPath.startsWith('/admin') && linkPath.startsWith('/admin')) {
                isActive = true;
            }

            if (isActive) {
                link.classList.add('is-active');
            }
        });
    }

    markActiveLinks('.main-nav a', { mode: 'prefix', allowAdminAlias: true });
    markActiveLinks('.admin-nav a', { mode: 'exact' });
});

document.addEventListener('DOMContentLoaded', function () {
    const cartButtons = document.querySelectorAll('.cart-quick-btn');
    if (!cartButtons.length) {
        return;
    }

    function updateCartBadge(count) {
        const badge = document.querySelector('.cart-nav-badge');
        if (!badge) {
            return;
        }
        if (!count || count <= 0) {
            badge.style.display = 'none';
            badge.textContent = '';
            return;
        }
        badge.style.display = 'inline-flex';
        badge.textContent = count;
    }

    function getDetailSelectedSizeId() {
        const active = document.querySelector('.size-options-detail .size-option.active');
        if (!active) {
            return null;
        }
        return active.dataset.sizeId || null;
    }

    cartButtons.forEach(button => {
        button.addEventListener('click', function (event) {
            event.preventDefault();
            event.stopPropagation();
            const productId = button.dataset.productId;
            let sizeId = button.dataset.sizeId || null;
            if (button.dataset.context === 'detail') {
                sizeId = getDetailSelectedSizeId();
            }
            if (!productId || !sizeId) {
                alert('Vui lòng chọn size trước khi thêm vào giỏ hàng.');
                return;
            }
            fetch('/cart/add', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: new URLSearchParams({
                    productId: productId,
                    sizeId: sizeId,
                    quantity: 1
                })
            })
                .then(async (response) => {
                    const data = await response.json().catch(() => ({}));
                    if (!response.ok) {
                        throw new Error(data.message || 'Không thể thêm vào giỏ hàng.');
                    }
                    if (data.distinctCount !== undefined) {
                        updateCartBadge(data.distinctCount);
                    }
                    button.classList.add('in-cart');
                })
                .catch((error) => {
                    alert(error.message || 'Không thể thêm vào giỏ hàng.');
                });
        });
    });
});

document.addEventListener('DOMContentLoaded', function () {
    const filterForms = document.querySelectorAll('form.filters');
    if (!filterForms.length) {
        return;
    }

    const autoApplyFields = new Set(['categoryId', 'priceRange', 'sort']);

    filterForms.forEach(form => {
        const selects = Array.from(form.querySelectorAll('select'));
        if (!selects.length) {
            return;
        }
        selects.forEach(select => {
            if (!autoApplyFields.has(select.name)) {
                return;
            }
            select.addEventListener('change', function () {
                const pageInput = form.querySelector('input[name="page"]');
                if (pageInput) {
                    pageInput.value = '0';
                }
                form.submit();
            });
        });
    });
});

document.addEventListener('DOMContentLoaded', function () {
    const cartForms = Array.from(document.querySelectorAll('.cart-item-form'));
    if (!cartForms.length) {
        return;
    }

    function clampCartQuantity(value) {
        let qty = parseInt(value || '1', 10);
        if (Number.isNaN(qty) || qty < 1) {
            qty = 1;
        }
        return qty;
    }

    cartForms.forEach(form => {
        const input = form.querySelector('input[name="quantity"]');
        const minusBtn = form.querySelector('[data-action="minus"]');
        const plusBtn = form.querySelector('[data-action="plus"]');

        if (!input) {
            return;
        }

        minusBtn && minusBtn.addEventListener('click', function () {
            const next = clampCartQuantity((parseInt(input.value || '1', 10) || 1) - 1);
            input.value = next;
        });

        plusBtn && plusBtn.addEventListener('click', function () {
            const next = clampCartQuantity((parseInt(input.value || '1', 10) || 1) + 1);
            input.value = next;
        });

        input.addEventListener('input', function () {
            input.value = clampCartQuantity(input.value);
        });
    });

    const checkoutBtn = document.querySelector('.checkout-btn');
    if (!checkoutBtn) {
        return;
    }

    checkoutBtn.addEventListener('click', function (event) {
        event.preventDefault();
        const targetUrl = checkoutBtn.getAttribute('href');

        const requests = cartForms.map(form => {
            const formData = new FormData(form);
            const params = new URLSearchParams(formData);
            return fetch(form.action, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: params
            }).then(response => {
                if (!response.ok) {
                    throw new Error('Cập nhật giỏ hàng thất bại.');
                }
            });
        });

        Promise.all(requests)
            .then(() => {
                window.location.href = targetUrl;
            })
            .catch((error) => {
                alert(error.message || 'Không thể cập nhật giỏ hàng.');
            });
    });
});

document.addEventListener('DOMContentLoaded', function () {
    const toggle = document.querySelector('.mobile-menu-toggle');
    const menu = document.querySelector('.mobile-menu');
    const backdrop = document.querySelector('.mobile-menu-backdrop');
    const categoryToggle = document.querySelector('.mobile-menu-toggle-btn');

    if (!toggle || !menu || !backdrop) {
        return;
    }

    function openMenu() {
        document.body.classList.add('mobile-menu-open');
        toggle.setAttribute('aria-expanded', 'true');
    }

    function closeMenu() {
        document.body.classList.remove('mobile-menu-open');
        toggle.setAttribute('aria-expanded', 'false');
    }

    toggle.addEventListener('click', function (event) {
        event.preventDefault();
        if (document.body.classList.contains('mobile-menu-open')) {
            closeMenu();
        } else {
            openMenu();
        }
    });

    backdrop.addEventListener('click', function () {
        closeMenu();
    });

    menu.addEventListener('click', function (event) {
        const link = event.target.closest('a');
        if (link) {
            closeMenu();
        }
    });

    if (categoryToggle) {
        categoryToggle.addEventListener('click', function (event) {
            event.preventDefault();
            const isOpen = document.body.classList.toggle('mobile-menu-categories-open');
            categoryToggle.setAttribute('aria-expanded', isOpen ? 'true' : 'false');
        });
    }
});
