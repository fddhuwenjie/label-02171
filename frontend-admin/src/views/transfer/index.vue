<template>
  <div class="transfer-page">
    <div class="search-card">
      <el-form :model="query" inline>
        <el-form-item label="调拨编号">
          <el-input v-model="query.transferNo" placeholder="请输入调拨编号" clearable @keyup.enter="handleSearch" style="width: 220px" />
        </el-form-item>
        <el-form-item label="调出机构">
          <el-select v-model="query.fromHospitalId" placeholder="全部" clearable filterable style="width: 220px">
            <el-option v-for="h in hospitalList" :key="h.id" :label="h.name" :value="h.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="调入机构">
          <el-select v-model="query.toHospitalId" placeholder="全部" clearable filterable style="width: 220px">
            <el-option v-for="h in hospitalList" :key="h.id" :label="h.name" :value="h.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 220px">
            <el-option label="待审批" value="PENDING" />
            <el-option label="已审批" value="APPROVED" />
            <el-option label="配送中" value="SHIPPING" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已驳回" value="REJECTED" />
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
        <span class="table-title">药品调拨列表</span>
        <el-button type="primary" @click="openCreateDialog">
          <el-icon><Plus /></el-icon>新建调拨单
        </el-button>
      </div>

      <el-table :data="tableData" stripe v-loading="loading" empty-text="暂无数据">
        <el-table-column prop="transferNo" label="调拨编号" width="160" />
        <el-table-column prop="fromHospitalName" label="调出机构" min-width="140" />
        <el-table-column prop="toHospitalName" label="调入机构" min-width="140" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagMap[row.status]?.type || 'info'" size="small">
              {{ statusTagMap[row.status]?.label || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openDetailDialog(row)">查看</el-button>
            <el-button v-if="row.status === 'PENDING'" type="success" link size="small" @click="handleApprove(row)">审批</el-button>
            <el-button v-if="row.status === 'PENDING'" type="danger" link size="small" @click="handleReject(row)">驳回</el-button>
            <el-button v-if="row.status === 'APPROVED'" type="primary" link size="small" @click="handleShip(row)">发货</el-button>
            <el-button v-if="row.status === 'SHIPPING'" type="success" link size="small" @click="handleComplete(row)">完成</el-button>
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

    <el-dialog v-model="createVisible" title="新建调拨单" width="780px" destroy-on-close>
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="调出机构" prop="fromHospitalId">
              <el-select v-model="createForm.fromHospitalId" placeholder="请选择调出机构" filterable style="width: 100%">
                <el-option v-for="h in hospitalList" :key="h.id" :label="h.name" :value="h.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="调入机构" prop="toHospitalId">
              <el-select v-model="createForm.toHospitalId" placeholder="请选择调入机构" filterable style="width: 100%">
                <el-option v-for="h in hospitalList" :key="h.id" :label="h.name" :value="h.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="createForm.remark" type="textarea" :rows="2" placeholder="备注信息（选填）" />
        </el-form-item>
        <el-form-item label="调拨明细">
          <el-table :data="createForm.items" border size="small" style="width: 100%">
            <el-table-column label="药品" min-width="180">
              <template #default="{ row }">
                <el-select v-model="row.drugId" placeholder="选择药品" filterable style="width: 100%">
                  <el-option v-for="d in drugList" :key="d.id" :label="d.name" :value="d.id" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="调出机构库存" width="130">
              <template #default="{ row }">
                <span v-if="!createForm.fromHospitalId || !row.drugId" class="text-muted">请先选择调出机构和药品</span>
                <span v-else-if="itemStockCache[stockKey(row)] === undefined" class="text-muted">加载中...</span>
                <span v-else :class="{ 'stock-warn': (itemStockCache[stockKey(row)] ?? 0) < (row.quantity ?? 0) }">
                  {{ itemStockCache[stockKey(row)] ?? 0 }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="数量" width="120">
              <template #default="{ row }">
                <el-input-number v-model="row.quantity" :min="1" :max="getMaxQuantity(row)" size="small" style="width: 100%" />
              </template>
            </el-table-column>
            <el-table-column label="批号" width="150">
              <template #default="{ row }">
                <el-input v-model="row.batchNo" placeholder="批号" size="small" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="70">
              <template #default="{ $index }">
                <el-button type="danger" link size="small" @click="removeItem($index)">移除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-button type="primary" link style="margin-top: 8px" @click="addItem">
            <el-icon><Plus /></el-icon> 添加药品
          </el-button>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleCreate">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="approveVisible" title="审批确认 - 库存核对" width="640px" destroy-on-close>
      <div v-if="approveTransferRow" class="approve-dialog">
        <el-alert v-if="approveInsufficient" type="error" :closable="false" show-icon style="margin-bottom: 16px">
          调出机构库存不足，无法审批通过。请驳回或等待库存补充后再审批。
        </el-alert>
        <el-descriptions :column="2" border size="small" style="margin-bottom: 16px">
          <el-descriptions-item label="调拨编号">{{ approveTransferRow.transferNo }}</el-descriptions-item>
          <el-descriptions-item label="调出机构">{{ approveTransferRow.fromHospitalName }}</el-descriptions-item>
          <el-descriptions-item label="调入机构">{{ approveTransferRow.toHospitalName }}</el-descriptions-item>
        </el-descriptions>
        <div class="info-title">各药品库存情况</div>
        <el-table :data="approveStockList" border size="small">
          <el-table-column prop="drugName" label="药品" min-width="120" />
          <el-table-column prop="quantity" label="调拨数量" width="100" />
          <el-table-column prop="fromStock" label="调出机构可用库存" width="130">
            <template #default="{ row }">
              <span :class="{ 'stock-warn': row.insufficient }">{{ row.fromStock ?? '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="toStock" label="调入机构库存" width="120" />
          <el-table-column label="是否充足" width="90">
            <template #default="{ row }">
              <el-tag v-if="row.insufficient" type="danger" size="small">不足</el-tag>
              <el-tag v-else type="success" size="small">充足</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <el-button @click="approveVisible = false">取消</el-button>
        <el-button type="danger" link @click="doReject">驳回</el-button>
        <el-button type="primary" :disabled="approveInsufficient" :loading="approveLoading" @click="doApprove">
          同意审批
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="调拨单详情" width="700px" destroy-on-close>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="调拨编号">{{ detail.transferNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagMap[detail.status]?.type || 'info'" size="small">
            {{ statusTagMap[detail.status]?.label || detail.status }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="调出机构">{{ detail.fromHospitalName }}</el-descriptions-item>
        <el-descriptions-item label="调入机构">{{ detail.toHospitalName }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detail.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ detail.remark || '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-table :data="detail.items || []" border size="small" style="margin-top: 16px">
        <el-table-column prop="drugName" label="药品名称" min-width="140" />
        <el-table-column prop="quantity" label="数量" width="100" />
        <el-table-column prop="batchNo" label="批号" width="130" />
      </el-table>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { getTransferPage, getTransfer, createTransfer, approveTransfer, rejectTransfer, shipTransfer, completeTransfer } from '@/api/transfer'
import { getHospitalList } from '@/api/hospital'
import { getDrugList } from '@/api/drug'
import { getInventoryQuantity, getAvailableQuantity } from '@/api/inventory'
import { ElMessage, ElMessageBox } from 'element-plus'

const statusTagMap = {
  PENDING: { label: '待审批', type: 'warning' },
  APPROVED: { label: '已审批', type: '' },
  SHIPPING: { label: '配送中', type: 'info' },
  COMPLETED: { label: '已完成', type: 'success' },
  REJECTED: { label: '已驳回', type: 'danger' }
}

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const hospitalList = ref([])
const drugList = ref([])

const query = reactive({ transferNo: '', fromHospitalId: '', toHospitalId: '', status: '', page: 1, size: 10 })

const createVisible = ref(false)
const submitLoading = ref(false)
const createFormRef = ref(null)

const createForm = reactive({
  fromHospitalId: null,
  toHospitalId: null,
  remark: '',
  items: [{ drugId: null, quantity: 1, batchNo: '' }]
})

const createRules = {
  fromHospitalId: [{ required: true, message: '请选择调出机构', trigger: 'change' }],
  toHospitalId: [{ required: true, message: '请选择调入机构', trigger: 'change' }]
}

const detailVisible = ref(false)
const detail = reactive({
  transferNo: '', fromHospitalName: '', toHospitalName: '', status: '',
  createdAt: '', remark: '', items: []
})

const itemStockCache = reactive({})

function stockKey(row) {
  return `${row.drugId ?? ''}-${createForm.fromHospitalId ?? ''}`
}

function getMaxQuantity(row) {
  const avail = itemStockCache[stockKey(row)]
  return avail != null ? Math.max(1, avail) : 99999
}

watch(
  () => ({ from: createForm.fromHospitalId, items: createForm.items }),
  () => {
    if (!createForm.fromHospitalId) return
    for (const item of createForm.items) {
      if (item.drugId) fetchItemStock(item.drugId, createForm.fromHospitalId)
    }
  },
  { deep: true }
)

async function fetchItemStock(drugId, hospitalId) {
  if (!drugId || !hospitalId) return
  const key = `${drugId}-${hospitalId}`
  try {
    const res = await getAvailableQuantity(drugId, hospitalId, null)
    itemStockCache[key] = res.data ?? 0
  } catch {
    itemStockCache[key] = null
  }
}

onMounted(() => {
  fetchData()
  fetchHospitalList()
  fetchDrugList()
})

async function fetchData() {
  loading.value = true
  try {
    const params = { page: query.page, size: query.size }
    if (query.transferNo) params.transferNo = query.transferNo
    if (query.fromHospitalId) params.fromHospitalId = query.fromHospitalId
    if (query.toHospitalId) params.toHospitalId = query.toHospitalId
    if (query.status) params.status = query.status
    const res = await getTransferPage(params)
    tableData.value = res.data?.records ?? []
    total.value = res.data?.total ?? 0
  } finally {
    loading.value = false
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

async function fetchDrugList() {
  try {
    const res = await getDrugList()
    drugList.value = res.data
  } catch {
    /* handled */
  }
}

function handleSearch() {
  query.page = 1
  fetchData()
}

function handleReset() {
  query.transferNo = ''
  query.fromHospitalId = ''
  query.toHospitalId = ''
  query.status = ''
  query.page = 1
  fetchData()
}

function openCreateDialog() {
  createForm.fromHospitalId = null
  createForm.toHospitalId = null
  createForm.remark = ''
  createForm.items = [{ drugId: null, quantity: 1, batchNo: '' }]
  Object.keys(itemStockCache).forEach(k => delete itemStockCache[k])
  createVisible.value = true
}

function addItem() {
  createForm.items.push({ drugId: null, quantity: 1, batchNo: '' })
}

function removeItem(index) {
  if (createForm.items.length <= 1) {
    ElMessage.warning('至少保留一项药品')
    return
  }
  createForm.items.splice(index, 1)
}

async function handleCreate() {
  const valid = await createFormRef.value.validate().catch(() => false)
  if (!valid) return
  if (createForm.fromHospitalId === createForm.toHospitalId) {
    ElMessage.warning('调出机构与调入机构不能相同')
    return
  }
  const hasEmpty = createForm.items.some(item => !item.drugId)
  if (hasEmpty) {
    ElMessage.warning('请选择所有药品')
    return
  }
  const drugQtySum = {}
  for (const item of createForm.items) {
    drugQtySum[item.drugId] = (drugQtySum[item.drugId] || 0) + (item.quantity || 0)
  }
  for (const [drugId, totalQty] of Object.entries(drugQtySum)) {
    const avail = itemStockCache[`${drugId}-${createForm.fromHospitalId}`]
    if (avail != null && totalQty > avail) {
      const drug = drugList.value.find(d => d.id == drugId)
      ElMessage.warning(`药品「${drug?.name || drugId}」调拨数量 ${totalQty} 超过调出机构可用库存 ${avail}`)
      return
    }
  }
  submitLoading.value = true
  try {
    await createTransfer({
      fromHospitalId: createForm.fromHospitalId,
      toHospitalId: createForm.toHospitalId,
      remark: createForm.remark,
      items: createForm.items
    })
    ElMessage.success('调拨单创建成功')
    createVisible.value = false
    fetchData()
  } finally {
    submitLoading.value = false
  }
}

async function openDetailDialog(row) {
  try {
    const res = await getTransfer(row.id)
    Object.assign(detail, res.data)
    detailVisible.value = true
  } catch {
    /* handled */
  }
}

const approveVisible = ref(false)
const approveTransferRow = ref(null)
const approveStockList = ref([])
const approveInsufficient = ref(false)
const approveLoading = ref(false)

async function handleApprove(row) {
  approveTransferRow.value = row
  approveVisible.value = true
  await loadApproveStock(row)
}

async function handleReject(row) {
  approveTransferRow.value = row
  approveVisible.value = true
  await loadApproveStock(row)
}

async function loadApproveStock(row) {
  approveStockList.value = []
  approveInsufficient.value = false
  try {
    const res = await getTransfer(row.id)
    const items = res.data?.items ?? []
    const list = []
    for (const item of items) {
      const [fromAvailRes, toRes] = await Promise.all([
        getAvailableQuantity(item.drugId, row.fromHospitalId, row.id),
        getInventoryQuantity(item.drugId, row.toHospitalId)
      ])
      const fromStock = fromAvailRes.data ?? 0
      const toStock = toRes.data ?? 0
      const insufficient = fromStock < (item.quantity ?? 0)
      if (insufficient) approveInsufficient.value = true
      list.push({
        drugName: item.drugName,
        quantity: item.quantity,
        fromStock,
        toStock,
        insufficient
      })
    }
    approveStockList.value = list
  } catch {
    approveStockList.value = []
  }
}

async function doApprove() {
  if (approveInsufficient.value) return
  approveLoading.value = true
  try {
    await approveTransfer(approveTransferRow.value.id)
    ElMessage.success('审批通过')
    approveVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '审批失败')
  } finally {
    approveLoading.value = false
  }
}

async function doReject() {
  try {
    await rejectTransfer(approveTransferRow.value.id)
    ElMessage.success('已驳回')
    approveVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '驳回失败')
  }
}

async function handleShip(row) {
  try {
    await ElMessageBox.confirm(`确定发货调拨单「${row.transferNo}」吗？`, '发货确认', {
      confirmButtonText: '确定', cancelButtonText: '取消', type: 'info'
    })
    await shipTransfer(row.id)
    ElMessage.success('已发货')
    fetchData()
  } catch {
    /* cancelled */
  }
}

async function handleComplete(row) {
  try {
    await ElMessageBox.confirm(`确定完成调拨单「${row.transferNo}」的收货确认吗？`, '完成确认', {
      confirmButtonText: '确定', cancelButtonText: '取消', type: 'info'
    })
    await completeTransfer(row.id)
    ElMessage.success('调拨完成')
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

.text-muted {
  color: #999;
  font-size: 12px;
}

.stock-warn {
  color: #f5222d;
  font-weight: 600;
}

.approve-dialog .info-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #333;
}
</style>
