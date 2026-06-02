import request from './request'

/** Send message to AI assistant */
export function sendMessage(message) {
  return request.post('/ai/chat', { message })
}

/** Get AI configuration status */
export function getAiConfig() {
  return request.get('/ai/config')
}

/** Update AI API key */
export function updateAiConfig(apiKey) {
  return request.put('/ai/config', { apiKey })
}
