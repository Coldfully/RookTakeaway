// 查询列表接口
const getDishPage = (params) => {
    return $axios({
        url: '/dish/page',
        method: 'get',
        params
    })
}

// 删除接口
const deleteDish = (ids) => {
    return $axios({
        url: '/dish',
        method: 'delete',
        params: {ids}
    })
}

// 修改接口
const editDish = (params) => {
    return $axios({
        url: '/dish',
        method: 'put',
        data: {...params}
    })
}

// 新增接口
const addDish = (params) => {
    return $axios({
        url: '/dish',
        method: 'post',
        data: {...params}
    })
}

// 查询详情
const queryDishById = (id) => {
    return $axios({
        url: `/dish/${id}`,
        method: 'get'
    })
}

// 获取菜品分类列表
const getCategoryList = (params) => {
    return $axios({
        url: '/category/list',
        method: 'get',
        params
    })
}

// 查菜品列表的接口
const queryDishList = (params) => {
    return $axios({
        url: '/dish/list',
        method: 'get',
        params
    })
}

// 文件down预览
const commonDownload = (params) => {
    return $axios({
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
        },
        url: '/common/download',
        method: 'get',
        params
    })
}

// 起售停售---批量起售停售接口
const dishStatusByStatus = (params) => {
    return $axios({
        url: `/dish/status/${params.status}`,
        method: 'post',
        params: {ids: params.id}
    })
}

// 菜品推荐列表
const foodRecommendListApi = (params) => {
    return $axios({
        url: '/backend/food/recommend/page',
        method: 'get',
        params
    })
}

// 更新菜品推荐
const updateFoodRecommendApi = (id) => {
    return $axios({
        url: `/backend/food/recommend/${id}`,
        method: 'post'
    })
}