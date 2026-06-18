<template>
  <!-- 路由出口: 使用过渡动画, keep-alive 缓存首页聊天记录 -->
  <router-view v-slot="{ Component }">
    <keep-alive include="Home">
      <component :is="Component" />
    </keep-alive>
  </router-view>
</template>

<script setup>
// 根组件: 仅承载路由出口
</script>
