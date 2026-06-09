<template>
  <div class="expiry-alert-page">
    <div class="search-card">
      <el-form :model="query" inline>
        <el-form-item label="预警等级">
          <el-select v-model="query.alertLevel" placeholder="全部" clearable style="width: 180px">
            <el-option label="临期(<90天)" value="WARNING" />
            <el-option label="紧急(<30天)" value="CRITICAL" />
            <el-option label="已过期" value="EXPIRED" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 160px">
            <el-option label="未处理" :value="1" />
            <el-option label="已处理" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="warning" @click="handleScan" :loading="scanLoading">立即扫描</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-card">
      <div class="table-header">
        <span class="table-title">效期预警列表</span>
        <span class="header-tip">系统每日凌晨 02:00 自动扫描，距离过期不足 90 天的库存批次将进入预警</span>
      </div>

      <el-table :data="tableData" stripe v-loading="loading" empty-text="暂无预警">
        <el-table-column prop="drugName" label="药品名称" min-width="160" />
        <el-table-column prop="drugCode" label="药品编码" width="140" />
        <el-table-column prop="spec" label="规格" width="120" />
        <el-table-column prop="hospitalName" label="所属机构" min-width="140" />
        <el-table-column prop="batchNo" label="批号" width="130" />
        <el-table-column prop="expireDate" label="过期日期" width="130" />
        <el-table-column label="剩余天数" width="120">
          <template #default="{ row }">
            <span :class="getDaysClass(row)">{{ row.daysToExpire }} 天</span>
          </template>
        </el-table-column>
        <el-table-column label="预警等级" width="120">
          <template #default="{ row }">
            <el-tag :type="levelTagMap[row.alertLevel]?.type || 'warning'" size="small">
              {{ levelTagMap[row.alertLevel]?.label || row.alertLevel }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'danger' : 'success'" size="small">
              {{ row.status === 1 ? '未处理' : '已处理' }}
            </el-tag>
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
import { ElMessage } from 'element-plus'
import { getExpiryAlertPage, triggerExpiryScan } from '@/api/expiryAlert'

const levelTagMap = {
  WARNING: { label: '临期', type: 'warning' },
  CRITICAL: { label: '紧急', type: 'danger' },
  EXPIRED: { label: '已过期', type: 'danger' }
}

const loading = ref(false)
const scanLoading = ref(false)
const tableData = ref([])
const total = ref(0)

const query = reactive({
  alertLevel: '',
  status: 1,
  page: 1,
  size: 10
})

onMounted(fetchData)

async function fetchData() {
  loading.value = true
  try {
    const params = { page: query.page, size: query.size }
    if (query.alertLevel) params.alertLevel = query.alertLevel
    if (query.status !== '' && query.status !== null && query.status !== undefined) params.status = query.status
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
  query.alertLevel = ''
  query.status = 1
  query.page = 1
  fetchData()
}

async function handleScan() {
  scanLoading.value = true
  try {
    const res = await triggerExpiryScan()
    ElMessage.success(`扫描完成，处理 ${res.data ?? 0} 条记录`)
    fetchData()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '扫描失败')
  } finally {
    scanLoading.value = false
  }
}

function getDaysClass(row) {
  if (row.daysToExpire < 0) return 'days-expired'
  if (row.daysToExpire < 30) return 'days-critical'
  return 'days-warning'
}
</script>

<style lang="scss" scoped>
.expiry-alert-page {
  .table-title {
    font-size: 16px;
    font-weight: 600;
    color: #333;
  }
  .table-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    flex-wrap: wrap;
    gap: 12px;
  }
  .header-tip {
    font-size: 12px;
    color: #999;
  }
  .days-warning {
    color: #faad14;
    font-weight: 600;
  }
  .days-critical {
    color: #f5222d;
    font-weight: 700;
  }
  .days-expired {
    color: #a8071a;
    font-weight: 700;
    text-decoration: line-through;
  }
}
</style>
