package com.jy.flsh.config

/**
 * 应用全局配置
 *
 * 多 URL 配置：默认地址 + 备用地址列表 + 白名单
 * 修改后需重新编译打包（写死配置）
 */
object AppConfig {

    /**
     * 默认入口地址（App 启动后加载的第一个页面）
     *
     * 必须带协议头（http://）：WebView 会把不带协议头的 "localhost:5174"
     * 当成未知 Scheme，直接回调 onReceivedError 并显示错误页。
     *
     * 设备上的 localhost 指向设备自身，访问开发机需先做端口反向映射：
     *   adb reverse tcp:5174 tcp:5174
     * 模拟器也可以直接使用 http://10.9.4.37:5174 访问宿主机。
     */
    const val DEFAULT_URL = "http://localhost:5174"

    /** 备用地址列表：默认地址不可达时按顺序尝试 */
    val FALLBACK_URLS = listOf(
        // 模拟器访问宿主机的固定地址（真机上不可达，会快速失败后继续下一个）
        "http://10.9.4.37:5174",
        "https://env-00jy72lhrujw-static.normal.cloudstatic.cn"
    )

    /**
     * URL 白名单：仅这些域名下的页面允许调用原生桥接能力
     *
     * 若改用局域网 IP 联调（如 http://192.168.1.10:5174），需把该 IP 同步加入此列表，
     * 否则页面内的跳转会被 shouldOverrideUrlLoading 拦截。
     */
    val ALLOWED_HOSTS = listOf(
        "localhost",
        "127.0.0.1",
        "10.9.4.37",
        "env-00jy72lhrujw-static.normal.cloudstatic.cn",
        "app.example.com",
        "backup.example.com"
    )

    /** 是否允许 HTTP（内网/测试环境用，生产建议仅 HTTPS） */
    const val ALLOW_HTTP = true

    /** 加载超时（毫秒） */
    const val LOAD_TIMEOUT_MS = 15_000L
}