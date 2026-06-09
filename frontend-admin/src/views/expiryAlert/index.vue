<template>
  <div class="expiry-alert-page">
    <div class="search-card">
      <el-form :model="query" inline>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 220px">
            <el-option label="生效中" value="ACTIVE" />
            <el-option label="已处理" value="RESOLVED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-card">
      <div class="table-header">
        <span class="table-title">效期预警列表</span>
        <el-button type="warning" @click="handleScan" :loading="scanLoading">
          <el-icon><Refresh /></el-icon>手动扫描
        </el-button>
      </div>

      <el-table :data="tableData" stripe v-loading="loading" empty-text="暂无预警数据">
        <el-table-column prop="drugName" label="药品名称" min-width="140" />
        <el-table-column prop="drugCode" label="药品编码" width="120" />
        <el-table-column prop="spec" label="规格" width="120" />
        <el-table-column prop="hospitalName" label="所属机构" min-width="140" />
        <el-table-column prop="batchNo" label="批号" width="130" />
        <el-table-column prop="expireDate" label="过期日期" width="120" />
        <el-table-column prop="daysUntilExpiry" label="距过期天数" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.daysUntilExpiry <= 30" type="danger" size="small" effect="dark">
              {{ row.daysUntilExpiry }}天
            </el-tag>
            <el-tag v-else-if="row.daysUntilExpiry <= 60" type="warning" size="small" effect="dark">
              {{ row.daysUntilExpiry }}天
            </el-tag>
            <el-tag v-else type="info" size="small">
              {{ row.daysUntilExpiry }}天
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'ACTIVE'" type="danger" size="small" effect="dark">生效中</el-tag>
            <el-tag v-else type="success" size="small">已处理</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="预警时间" width="170" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'ACTIVE'" type="primary" link size="small" @click="handleResolve(row)">处理</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getExpiryAlertPage, scanExpiryAlerts, resolveAlert } from '@/api/expiryAlert'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'

const loading = ref(false)
const scanLoading = ref(false)
const tableData = ref([])
const total = ref(0)

const query = reactive({ status: '', page: 1, size: 10 })

onMounted(() => {
  fetchData()
})

async function fetchData() {
  loading.value = true
  try {
    const params = { page: query.page, size: query.size }
    if (query.status) params.status = query.status
    const res = await getExpiryAlertPage(params)
    tableData.value = res.data?.records ?? []
    total.value = res.data?.total ?? 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.page = 1
  fetchData()
}

function handleReset() {
  query.status = ''
  query.page = 1
  fetchData()
}

async function handleScan() {
  scanLoading.value = true
  try {
    await scanExpiryAlerts()
    ElMessage.success('扫描完成')
    fetchData()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '扫描失败')
  } finally {
    scanLoading.value = false
  }
}

async function handleResolve(row) {
  try {
    await ElMessageBox.confirm('确定将该预警标记为已处理吗？', '处理确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await resolveAlert(row.id)
    ElMessage.success('已处理')
    fetchData()
  } catch {
    /* cancelled */
  }
}
</script>

<style lang="scss" scoped>
.table-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}
</style>
