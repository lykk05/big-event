import request from '@/utils/request.js'

export const aiGenerateService = (data) => {
    return request.post('/ai/writing/generate', data)
}