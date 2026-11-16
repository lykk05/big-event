import request from '@/utils/request.js'

export const aiGenerateService = (data, config = {}) => {
    return request.post('/ai/writing/generate', data, config)
}
