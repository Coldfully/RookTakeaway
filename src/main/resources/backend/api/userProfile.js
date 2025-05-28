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
    // 确保id是数字类型
    const userId = parseInt(id);
    if (isNaN(userId) || userId <= 0) {
        return Promise.reject(new Error('无效的用户ID'));
    }
    
    return $axios({
        url: `/backend/user/profile/generate/${userId}`,
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

// 删除用户画像
const deleteUserProfileApi = (id) => {
    return $axios({
        url: `/backend/user/profile/${id}`,
        method: 'delete'
    })
} 