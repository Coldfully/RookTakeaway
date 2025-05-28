new Vue({
    el: '#userProfile',
    data() {
        return {
            loading: false,
            error: false,
            errorMessage: '',
            profileData: null
        }
    },
    created() {
        this.loadUserProfile();
    },
    methods: {
        goBack() {
            history.go(-1);
        },
        loadUserProfile() {
            this.loading = true;
            this.error = false;
            
            $.ajax({
                url: '/user/profile',
                method: 'GET',
                success: (res) => {
                    this.loading = false;
                    if (res.code === 1) {
                        this.displayProfile(res.data);
                    } else {
                        this.showError(res.msg || '获取用户画像失败');
                    }
                },
                error: () => {
                    this.loading = false;
                    this.showError('获取用户画像失败，请稍后重试');
                }
            });
        },
        generateProfile() {
            this.loading = true;
            this.error = false;
            
            $.ajax({
                url: '/user/profile/generate',
                method: 'POST',
                success: (res) => {
                    this.loading = false;
                    if (res.code === 1) {
                        this.displayProfile(res.data);
                    } else {
                        this.showError(res.msg || '生成用户画像失败');
                    }
                },
                error: () => {
                    this.loading = false;
                    this.showError('生成用户画像失败，请稍后重试');
                }
            });
        },
        displayProfile(data) {
            if (!data) {
                this.showError('暂无用户画像数据');
                return;
            }

            this.profileData = data;
            $('#favoriteDishes').html(this.formatFavoriteDishes(data.favoriteDishes));
            $('#tastePreference').html(data.tastePreference || '暂无数据');
            $('#consumptionHabits').html(data.consumptionHabits || '暂无数据');
            $('#aiAnalysis').html(this.formatAIAnalysis(data.aiAnalysis));
            
            $('.profile-content').show();
            $('#error').hide();
        },
        formatFavoriteDishes(dishes) {
            if (!dishes) return '暂无数据';
            return dishes.split(',').map(dish => `<div class="dish-item">${dish}</div>`).join('');
        },
        formatAIAnalysis(analysis) {
            if (!analysis) return '暂无数据';
            return analysis.split('\n').map(line => `<p>${line}</p>`).join('');
        },
        showError(message) {
            this.error = true;
            this.errorMessage = message;
            $('#error p').text(message);
            $('#error').show();
            $('.profile-content').hide();
        }
    },
    mounted() {
        $('#generateProfile').click(() => {
            this.generateProfile();
        });
    }
}); 