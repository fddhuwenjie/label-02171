<template>
  <div class="log-page">
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
        <el-form-item label="类型">
          <el-select v-model="query.type" placeholder="全部" clearable style="width: 220px">
            <el-option label="入库" value="IN" />
            <el-option label="出库" value="OUT" />
            <el-option label="调入" value="TRANSFER_IN" />
            <el-option label="调出" value="TRANSFER_OUT" />
            <el-option label="调整" value="ADJUST" />
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
        <span class="table-title">库存日志</span>
      </div>

      <el-table :data="tableData" stripe v-loading="loading" empty-text="暂无数据">
        <el-table-column prop="drugName" label="药品名称" min-width="140" />
        <el-table-column prop="hospitalName" label="所属机构" min-width="140" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="typeTagMap[row.type]?.type || 'info'" size="small">
              {{ typeTagMap[row.type]?.label || row.type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="100">
          <template #default="{ row }">
            <span :style="{ color: row.quantity > 0 ? '#52C41A' : '#F5222D' }">
              {{ row.quantity > 0 ? `+${row.quantity}` : row.quantity }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="batchNo" label="批号" width="120" />
        <el-table-column prop="operator" label="操作人" width="100" />
        <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="操作时间" width="170" />
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
import { getInventoryLogPage } from '@/api/log'
import { getDrugList } from '@/api/drug'
import { getHospitalList } from '@/api/hospital'

const typeTagMap = {
  IN: { label: '入库', type: 'success' },
  OUT: { label: '出库', type: 'danger' },
  TRANSFER_IN: { label: '调入', type: '' },
  TRANSFER_OUT: { label: '调出', type: 'warning' },
  ADJUST: { label: '调整', type: 'info' }
}

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const drugList = ref([])
const hospitalList = ref([])

const query = reactive({ drugId: '', hospitalId: '', type: '', page: 1, size: 10 })

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
    if (query.type) params.type = query.type
    const res = await getInventoryLogPage(params)
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
  query.type = ''
  query.page = 1
  fetchData()
}
</script>

<style lang="scss" scoped>
.table-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}
</style>
