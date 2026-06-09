<template>
  <div class="expiry-alert-page">
    <div class="search-card">
      <el-form :model="query" inline>
        <el-form-item label="机构">
          <el-select v-model="query.hospitalId" placeholder="全部" clearable filterable style="width: 220px">
            <el-option v-for="h in hospitalList" :key="h.id" :label="h.name" :value="h.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="预警级别">
          <el-select v-model="query.alertLevel" placeholder="全部" clearable style="width: 180px">
            <el-option label="红色预警（30天内）" value="RED" />
            <el-option label="黄色预警（31-90天）" value="YELLOW" />
            <el-option label="已过期" value="EXPIRED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="warning" @click="handleScan">立即扫描</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-card">
      <div class="table-header">
        <span class="table-title">效期预警列表</span>
        <div class="header-stats">
          <el-tag type="danger" effect="dark">红色 {{ redCount }}</el-tag>
          <el-tag type="warning" effect="dark">黄色 {{ yellowCount }}</el-tag>
          <el-tag type="info" effect="dark">过期 {{ expiredCount }}</el-tag>
        </div>
      </div>

      <el-table :data="tableData" stripe v-loading="loading" empty-text="暂无预警数据">
        <el-table-column label="预警级别" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="levelTagMap[row.alertLevel]?.type" effect="dark" size="small">
              {{ levelTagMap[row.alertLevel]?.label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="drugName" label="药品名称" min-width="160" />
        <el-table-column prop="drugCode" label="药品编码" width="130" />
        <el-table-column prop="spec" label="规格" width="110" />
        <el-table-column prop="hospitalName" label="所在机构" min-width="140" />
        <el-table-column prop="batchNo" label="批号" width="120" />
        <el-table-column prop="quantity" label="库存数量" width="100" align="right" />
        <el-table-column prop="expireDate" label="过期日期" width="120" />
        <el-table-column label="距过期天数" width="110" align="center">
          <template #default="{ row }">
            <span :class="daysClass(row.daysToExpire)">
              {{ row.daysToExpire >= 0 ? `${row.daysToExpire}天` : `已过期${-row.daysToExpire}天` }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleResolve(row)">标记已处理</el-button>
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
import { ref, reactive, computed, onMounted } from 'vue'
import { getExpiryAlertPage, resolveExpiryAlert, triggerExpiryScan } from '@/api/expiryAlert'
import { getHospitalList } from '@/api/hospital'
import { ElMessage, ElMessageBox } from 'element-plus'

const levelTagMap = {
  RED: { label: '红色预警', type: 'danger' },
  YELLOW: { label: '黄色预警', type: 'warning' },
  EXPIRED: { label: '已过期', type: 'info' }
}

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const hospitalList = ref([])

const query = reactive({
  hospitalId: '',
  alertLevel: '',
  page: 1,
  size: 10
})

const redCount = computed(() => tableData.value.filter(r => r.alertLevel === 'RED').length)
const yellowCount = computed(() => tableData.value.filter(r => r.alertLevel === 'YELLOW').length)
const expiredCount = computed(() => tableData.value.filter(r => r.alertLevel === 'EXPIRED').length)

function daysClass(days) {
  if (days < 0) return 'days-expired'
  if (days <= 30) return 'days-red'
  return 'days-yellow'
}

onMounted(() => {
  fetchData()
  fetchHospitals()
})

async function fetchData() {
  loading.value = true
  try {
    const params = { page: query.page, size: query.size }
    if (query.hospitalId) params.hospitalId = query.hospitalId
    if (query.alertLevel) params.alertLevel = query.alertLevel
    const res = await getExpiryAlertPage(params)
    tableData.value = res.data?.records ?? []
    total.value = res.data?.total ?? 0
  } finally {
    loading.value = false
  }
}

async function fetchHospitals() {
  try {
    const res = await getHospitalList()
    hospitalList.value = res.data
  } catch {
    /* handled */
  }
}

function handleSearch() {
  query.page = 1
  fetchData()
}

function handleReset() {
  query.hospitalId = ''
  query.alertLevel = ''
  query.page = 1
  fetchData()
}

async function handleResolve(row) {
  try {
    await ElMessageBox.confirm(`确定将「${row.drugName}」的预警标记为已处理吗？`, '处理确认', {
      confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
    })
    await resolveExpiryAlert(row.id)
    ElMessage.success('已标记为处理')
    fetchData()
  } catch {
    /* cancelled */
  }
}

async function handleScan() {
  try {
    await ElMessageBox.confirm('确定立即执行效期扫描吗？系统会自动检查所有库存药品效期并生成预警。', '手动扫描', {
      confirmButtonText: '确定', cancelButtonText: '取消', type: 'info'
    })
    await triggerExpiryScan()
    ElMessage.success('扫描完成')
    fetchData()
  } catch {
    /* cancelled */
  }
}
</script>

<style lang="scss" scoped>
.days-red {
  color: #F5222D;
  font-weight: 700;
}
.days-yellow {
  color: #FAAD14;
  font-weight: 600;
}
.days-expired {
  color: #8C8C8C;
  font-weight: 700;
}
.table-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}
.header-stats {
  display: flex;
  gap: 8px;
}
</style>
