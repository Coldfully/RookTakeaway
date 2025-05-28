// 用户画像列表
const userProfileListApi = (params) => {
    return $axios({
        url: '/backend/user/profile/page',
        method: 'get',
        params
    })
}

// 生成用户画像
const generateUserProfileApi = (id) => {
    return $axios({
        url: `/backend/user/profile/generate/${id}`,
        method: 'post'
    })
}

// 批量生成用户画像
const generateBatchUserProfileApi = () => {
    return $axios({
        url: '/backend/user/profile/generate/batch',
        method: 'post'
    })
} 