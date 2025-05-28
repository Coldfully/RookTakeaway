(function (doc, win) {
    var docEl = doc.documentElement,
        resizeEvt = 'orientationchange' in window ? 'orientationchange' : 'resize',
        recalc = function () {
            var clientWidth = docEl.clientWidth;
            if (!clientWidth) return;
            if (clientWidth > 750) {
                docEl.style.fontSize = '28px';
            } else {
                docEl.style.fontSize = (clientWidth / 375) + 'px';
            }
        };

    if (!doc.addEventListener) return;
    win.addEventListener(resizeEvt, recalc, false);
    doc.addEventListener('DOMContentLoaded', recalc, false);

    // 设备检测和重定向逻辑
    function checkDeviceAndRedirect() {
        var currentPath = window.location.pathname;
        var isDesktop = currentPath.includes('desktop.html');
        var isMobile = currentPath.includes('index.html');
        var isDesktopDevice = window.innerWidth > 750;

        // 如果在desktop.html页面，且是移动设备，则重定向到index.html
        if (isDesktop && !isDesktopDevice) {
            window.location.href = '/front/index.html';
        }
        // 如果在index.html页面，且是桌面设备，则重定向到desktop.html
        else if (isMobile && isDesktopDevice) {
            window.location.href = '/front/page/desktop.html';
        }
    }

    // 页面加载时检查
    win.addEventListener('load', checkDeviceAndRedirect);
    // 窗口大小改变时检查
    win.addEventListener('resize', checkDeviceAndRedirect);
})(document, window);