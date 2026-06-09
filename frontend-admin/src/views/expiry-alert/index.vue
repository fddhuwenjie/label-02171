<template>
  <div class="expiry-alert-page">
    <div class="search-card">
      <el-form :model="query" inline>
        <el-form-item label="药品">
          <el-select v-model="query.drugId" placeholder="全部药品" clearable filterable style="width: 220px">
            <el-option v-for="d in drugList" :key="d.id" :label="d.name" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="机构">
          <el-select v-model="query.hospitalId" placeholder="全部机构" clearable filterable style="width: 220px">
            <el-option v-for="h in hospitalList" :key="h.id" :label="h.name" :value="h.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="预警级别">
          <el-select v-model="query.alertLevel" placeholder="全部" clearable style="width: 150px">
            <el-option label="预警" value="WARNING" />
            <el-option label="紧急" value="URGENT" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 150px">
            <el-option label="活跃" value="ACTIVE" />
            <el-option label="已解决" value="RESOLVED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="handleScan">
            <el-icon><Refresh /></el-icon> 立即扫描
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-card">
      <div class="table-header">
        <span class="table-title">效期预警列表</span>
      </div>

      <el-table :data="tableData" stripe v-loading="loading" empty-text="暂无数据">
        <el-table-column prop="drugName" label="药品名称" min-width="140" />
        <el-table-column prop="drugCode" label="药品编码" width="120" />
        <el-table-column prop="spec" label="规格" width="100" />
        <el-table-column prop="hospitalName" label="所属机构" min-width="140" />
        <el-table-column prop="batchNo" label="批号" width="120" />
        <el-table-column prop="expireDate" label="有效期至" width="120" />
        <el-table-column prop="daysLeft" label="剩余天数" width="100">
          <template #default="{ row }">
            <el-tag :type="row.daysLeft <= 30 ? 'danger' : 'warning'" size="small">
              {{ row.daysLeft }}天
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="库存数量" width="100" />
        <el-table-column prop="alertLevel" label="预警级别" width="100">
          <template #default="{ row }">
            <el-tag :type="row.alertLevel === 'URGENT' ? 'danger' : 'warning'" size="small">
              {{ row.alertLevel === 'URGENT' ? '紧急' : '预警' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'danger' : 'success'" size="small">
              {{ row.status === 'ACTIVE' ? '活跃' : '已解决' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'ACTIVE'"
              type="success"
              link
              size="small"
              @click="handleResolve(row)"
            >
              标记解决
            </el-button>
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
import { getExpiryAlertPage, scanExpiryAlerts, resolveExpiryAlert } from '@/api/expiryAlert'
import { getDrugList } from '@/api/drug'
import { getHospitalList } from '@/api/hospital'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const drugList = ref([])
const hospitalList = ref([])

const query = reactive({ drugId: '', hospitalId: '', alertLevel: '', status: 'ACTIVE', page: 1, size: 10 })

onMounted(() => {
  fetchData()
  fetchDrugList()
  fetchHospitalList()
})

async function fetchData() {
  loading.value = true
  try {
    const params = { page: query.page, size: query.size }
    if (query.drugId) params.drugId = query.drugId
    if (query.hospitalId) params.hospitalId = query.hospitalId
    if (query.alertLevel) params.alertLevel = query.alertLevel
    if (query.status) params.status = query.status
    const res = await getExpiryAlertPage(params)
    tableData.value = res.data?.records ?? []
    total.value = res.data?.total ?? 0
  } finally {
    loading.value = false
  }
}

async function fetchDrugList() {
  try {
    const res = await getDrugList()
    drugList.value = res.data
  } catch {
    /* handled */
  }
}

async function fetchHospitalList() {
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
  query.drugId = ''
  query.hospitalId = ''
  query.alertLevel = ''
  query.status = 'ACTIVE'
  query.page = 1
  fetchData()
}

async function handleScan() {
  try {
    await ElMessageBox.confirm('确定要立即执行效期预警扫描吗？', '扫描确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    await scanExpiryAlerts()
    ElMessage.success('扫描完成')
    fetchData()
  } catch {
    /* cancelled */
  }
}

async function handleResolve(row) {
  try {
    await ElMessageBox.confirm(`确定要将药品「${row.drugName}」的效期预警标记为已解决吗？`, '确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await resolveExpiryAlert(row.id)
    ElMessage.success('已标记为已解决')
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
