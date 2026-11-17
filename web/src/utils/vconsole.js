const STORAGE_KEY = 'vconsole_enabled'

function shouldEnable() {
  const params = new URLSearchParams(window.location.search)
  const flag = params.get('vconsole')

  if (flag === '0' || flag === 'false') {
    window.localStorage.removeItem(STORAGE_KEY)
    return false
  }
  if (flag === '1' || flag === 'true') {
    window.localStorage.setItem(STORAGE_KEY, '1')
    return true
  }
  if (import.meta.env.DEV) return true

  return window.localStorage.getItem(STORAGE_KEY) === '1'
}

export async function setupVConsole() {
  if (typeof window === 'undefined' || !shouldEnable()) return

  const { default: VConsole } = await import('vconsole')
  window.vConsole = new VConsole({ theme: 'dark' })
}
