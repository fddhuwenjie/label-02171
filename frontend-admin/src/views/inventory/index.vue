<template>
  <div class="inventory-page">
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
        <el-form-item label="">
          <el-checkbox v-model="query.warningOnly" label="仅显示预警" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-card">
      <div class="table-header">
        <span class="table-title">库存列表</span>
      </div>

      <el-table :data="tableData" stripe v-loading="loading" empty-text="暂无数据">
        <el-table-column prop="drugName" label="药品名称" min-width="140" />
        <el-table-column prop="hospitalName" label="所属机构" min-width="140" />
        <el-table-column prop="quantity" label="库存数量" width="110">
          <template #default="{ row }">
            <span :class="{ 'warning-text': row.quantity <= row.warningThreshold }">
              {{ row.quantity }}
            </span>
            <el-icon v-if="row.quantity <= row.warningThreshold" color="#F5222D" style="margin-left: 4px">
              <WarningFilled />
            </el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="warningThreshold" label="预警阈值" width="100" />
        <el-table-column prop="batchNo" label="批号" width="120" />
        <el-table-column prop="expireDate" label="有效期至" width="120" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openAdjustDialog(row)">
              库存调整
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

    <el-dialog v-model="adjustVisible" title="库存调整" width="560px" destroy-on-close>
      <div v-if="adjustForm.currentRow" class="adjust-info">
        <div class="info-title">当前库存信息</div>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="药品名称">{{ adjustForm.currentRow?.drugName }}</el-descriptions-item>
          <el-descriptions-item label="所属机构">{{ adjustForm.currentRow?.hospitalName }}</el-descriptions-item>
          <el-descriptions-item label="当前库存">
            <span :class="{ 'warning-text': adjustForm.currentRow?.quantity <= adjustForm.currentRow?.warningThreshold }">
              {{ adjustForm.currentRow?.quantity }}
            </span>
            <el-icon v-if="adjustForm.currentRow?.quantity <= adjustForm.currentRow?.warningThreshold" color="#F5222D" style="margin-left: 4px"> <WarningFilled /> </el-icon>
          </el-descriptions-item>
          <el-descriptions-item label="预警阈值">{{ adjustForm.currentRow?.warningThreshold }}</el-descriptions-item>
          <el-descriptions-item label="批号">{{ adjustForm.currentRow?.batchNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="有效期至">{{ adjustForm.currentRow?.expireDate || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <el-form ref="adjustFormRef" :model="adjustForm" :rules="adjustRules" label-width="100px">
        <el-form-item v-if="!adjustForm.currentRow" label="药品" prop="drugId">
          <el-select v-model="adjustForm.drugId" placeholder="请选择药品" filterable style="width: 100%">
            <el-option v-for="d in drugList" :key="d.id" :label="d.name" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!adjustForm.currentRow" label="机构" prop="hospitalId">
          <el-select v-model="adjustForm.hospitalId" placeholder="请选择机构" filterable style="width: 100%">
            <el-option v-for="h in hospitalList" :key="h.id" :label="h.name" :value="h.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="调整数量" prop="adjustQuantity">
          <el-input-number v-model="adjustForm.adjustQuantity" :step="1" :min="-99999" :max="99999" style="width: 100%" />
          <div class="form-tip">正数为入库，负数为出库</div>
        </el-form-item>
        <el-form-item label="调整后库存">
          <span class="result-quantity">{{ (adjustForm.currentRow?.quantity ?? 0) + (adjustForm.adjustQuantity ?? 0) }}</span>
        </el-form-item>
        <el-form-item label="批号" prop="batchNo">
          <el-input v-model="adjustForm.batchNo" placeholder="请输入批号" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="adjustForm.remark" type="textarea" :rows="3" placeholder="请输入调整原因（如：盘点差异、报损、补货等）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adjustVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleAdjust">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getInventoryPage, adjustInventory } from '@/api/inventory'
import { getDrugList } from '@/api/drug'
import { getHospitalList } from '@/api/hospital'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const drugList = ref([])
const hospitalList = ref([])

const query = reactive({ drugId: '', hospitalId: '', warningOnly: false, page: 1, size: 10 })

const adjustVisible = ref(false)
const submitLoading = ref(false)
const adjustFormRef = ref(null)

const adjustForm = reactive({
  drugId: null,
  hospitalId: null,
  adjustQuantity: 0,
  batchNo: '',
  remark: '',
  currentRow: null
})

const adjustRules = {
  drugId: [{ required: true, message: '请选择药品', trigger: 'change' }],
  hospitalId: [{ required: true, message: '请选择机构', trigger: 'change' }],
  adjustQuantity: [{ required: true, message: '请输入调整数量', trigger: 'blur' }]
}

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
    if (query.warningOnly) params.warningOnly = query.warningOnly
    const res = await getInventoryPage(params)
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
  query.warningOnly = false
  query.page = 1
  fetchData()
}

function openAdjustDialog(row) {
  if (row) {
    Object.assign(adjustForm, {
      drugId: row.drugId,
      hospitalId: row.hospitalId,
      adjustQuantity: 0,
      batchNo: row.batchNo || '',
      remark: '',
      currentRow: row
    })
  } else {
    Object.assign(adjustForm, {
      drugId: null,
      hospitalId: null,
      adjustQuantity: 0,
      batchNo: '',
      remark: '',
      currentRow: null
    })
  }
  adjustVisible.value = true
}

async function handleAdjust() {
  const valid = await adjustFormRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    const { currentRow, ...payload } = adjustForm
    await adjustInventory(payload)
    ElMessage.success('库存调整成功')
    adjustVisible.value = false
    fetchData()
  } finally {
    submitLoading.value = false
  }
}
</script>

<style lang="scss" scoped>
.table-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.warning-text {
  color: #F5222D;
  font-weight: 600;
}

.form-tip {
  font-size: 12px;
  color: #8C8C8C;
  margin-top: 4px;
}

.adjust-info {
  margin-bottom: 16px;
  padding: 12px;
  background: #fafafa;
  border-radius: 8px;

  .info-title {
    font-size: 14px;
    font-weight: 600;
    color: #333;
    margin-bottom: 12px;
  }
}

.result-quantity {
  font-size: 16px;
  font-weight: 600;
  color: #1677ff;
}
</style>
