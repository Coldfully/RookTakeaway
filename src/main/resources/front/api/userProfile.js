// 用户画像相关接口
import request from '@/utils/request'

// 获取用户画像
export function getUserProfile() {
  return request({
    url: '/user/profile',
    method: 'get'
  })
}

// 生成用户画像
export function generateUserProfile() {
  return request({
    url: '/user/profile/generate',
    method: 'post'
  })
} 