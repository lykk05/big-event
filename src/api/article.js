import request from '@/utils/request.js'

// 文章分类列表查询接口函数
export const articleCategoryListService = () => {
    return request.get('/category')
}

// 添加文章分类接口函数
export const articleCategoryAddService = (categoryData) => {
    return request.post('/category',categoryData)
}

// 添加更新文章分类接口函数
export const articleCategoryUpdateService = (categoryData) => {
    return request.put('/category',categoryData)
}

// 删除文章分类接口函数
export const articleCategoryDeleteService = (categoryId) => {
    return request.delete('/category?id='+categoryId)
}

// 文章列表查询接口函数
export const articleListService = (params) => {
    return request.get('/article',{params:params})
}

// 添加文章接口函数
export const articleAddService = (articleData) => {
    return request.post('/article',articleData)
}

// 更新文章接口函数
export const articleUpdateService = (articleData) => {
    return request.put('/article',articleData)
}

// 删除文章接口函数
export const articleDeleteService = (articleId) => {
    return request.delete('/article?id='+articleId)
}