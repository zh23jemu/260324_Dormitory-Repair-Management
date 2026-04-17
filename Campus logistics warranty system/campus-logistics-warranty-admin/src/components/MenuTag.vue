<template>
  <div class="tags">
    <el-tag 
    v-for="(tag,index) in tags" 
    :key="tag.name" 
    :closable="tag.name!=='home'" 
    :effect="route.name === tag.name ?'dark':'plain'"
    @click="handleClick(tag)"
    @close="handleClose(tag,index)"
    >
      {{ tag.label }}
    </el-tag>
  </div>
</template>

<script lang="ts" setup>
import {computed, ref } from 'vue'
import { useRoute,useRouter } from 'vue-router';
import {useStore} from '@/store/index'
const store = useStore()
const route = useRoute()
const router = useRouter()
const tags = computed(()=>{
  return store.state.tabsList
})

const handleClick = (tag) => {
  router.push(tag.path)
  store.addTags(tag)
}
const handleClose = (tag,index) => {
  store.deleteTags(tag,index)
  if(tag.name !== route.name){
    return
  }
  const nextTag = tags.value[index] || tags.value[index-1]
  console.log(nextTag);
  if(nextTag){
    router.push(nextTag.path)
  }else{
    router.push('/home')
  }
}
</script>
<style lang="less" scoped>
.tags{
  margin: 20px 0 0 20px;
  width: 100%;
 .el-tag{
    margin-right: 10px;
    cursor: pointer;
  }

}


</style>