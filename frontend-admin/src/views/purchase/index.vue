<template>
  <div class="purchase-page">
    <div class="search-card">
      <el-form :model="query" inline>
        <el-form-item label="订单编号">
          <el-input v-model="query.orderNo" placeholder="请输入订单编号" clearable @keyup.enter="handleSearch" style="width: 220px" />
        </el-form-item>
        <el-form-item label="机构">
          <el-select v-model="query.hospitalId" placeholder="全部机构" clearable filterable style="width: 220px">
            <el-option v-for="h in hospitalList" :key="h.id" :label="h.name" :value="h.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 220px">
            <el-option label="待审批" value="PENDING" />
            <el-option label="已审批" value="APPROVED" />
            <el-option label="已发货" value="DELIVERED" />
            <el-option label="已完成" value="COMPLETED" />
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
        <span class="table-title">采购订单列表</span>
        <el-button type="primary" @click="openCreateDialog">
          <el-icon><Plus /></el-icon>新建采购单
        </el-button>
      </div>

      <el-table :data="tableData" stripe v-loading="loading" empty-text="暂无数据">
        <el-table-column prop="orderNo" label="订单编号" width="160" />
        <el-table-column prop="hospitalName" label="采购机构" min-width="140" />
        <el-table-column prop="supplier" label="供应商" min-width="140" show-overflow-tooltip />
        <el-table-column prop="totalAmount" label="总金额(元)" width="110">
          <template #default="{ row }">
            {{ row.totalAmount != null ? `¥${Number(row.totalAmount).toFixed(2)}` : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagMap[row.status]?.type || 'info'" size="small">
              {{ statusTagMap[row.status]?.label || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openDetailDialog(row)">查看</el-button>
            <el-button v-if="row.status === 'PENDING'" type="success" link size="small" @click="handleApprove(row)">审批</el-button>
            <el-button v-if="row.status === 'APPROVED'" type="primary" link size="small" @click="handleReceive(row)">收货</el-button>
            <el-button v-if="row.status === 'PENDING'" type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="createVisible" title="新建采购单" width="780px" destroy-on-close>
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="采购机构" prop="hospitalId">
              <el-select v-model="createForm.hospitalId" placeholder="请选择机构" filterable style="width: 100%">
                <el-option v-for="h in hospitalList" :key="h.id" :label="h.name" :value="h.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="供应商" prop="supplier">
              <el-input v-model="createForm.supplier" placeholder="请输入供应商名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="createForm.remark" type="textarea" :rows="2" placeholder="备注信息（选填）" />
        </el-form-item>
        <el-form-item label="采购明细">
          <el-table :data="createForm.items" border size="small" style="width: 100%">
            <el-table-column label="药品" min-width="180">
              <template #default="{ row }">
                <el-select v-model="row.drugId" placeholder="选择药品" filterable style="width: 100%">
                  <el-option v-for="d in drugList" :key="d.id" :label="d.name" :value="d.id" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="数量" width="120">
              <template #default="{ row }">
                <el-input-number v-model="row.quantity" :min="1" size="small" style="width: 100%" />
              </template>
            </el-table-column>
            <el-table-column label="单价(元)" width="130">
              <template #default="{ row }">
                <el-input-number v-model="row.unitPrice" :min="0" :precision="2" size="small" style="width: 100%" />
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

    <el-dialog v-model="detailVisible" title="采购单详情" width="700px" destroy-on-close>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单编号">{{ detail.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="采购机构">{{ detail.hospitalName }}</el-descriptions-item>
        <el-descriptions-item label="供应商">{{ detail.supplier }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagMap[detail.status]?.type || 'info'" size="small">
            {{ statusTagMap[detail.status]?.label || detail.status }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="总金额">
          {{ detail.totalAmount != null ? `¥${Number(detail.totalAmount).toFixed(2)}` : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detail.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail.remark || '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-table :data="detail.items || []" border size="small" style="margin-top: 16px">
        <el-table-column prop="drugName" label="药品名称" min-width="140" />
        <el-table-column prop="quantity" label="数量" width="100" />
        <el-table-column prop="unitPrice" label="单价(元)" width="110">
          <template #default="{ row }">
            {{ row.unitPrice != null ? `¥${Number(row.unitPrice).toFixed(2)}` : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="小计(元)" width="110">
          <template #default="{ row }">
            {{ row.unitPrice != null && row.quantity != null ? `¥${(row.unitPrice * row.quantity).toFixed(2)}` : '-' }}
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getPurchaseOrderPage, getPurchaseOrder, createPurchaseOrder, approvePurchaseOrder, receivePurchaseOrder, deletePurchaseOrder } from '@/api/purchase'
import { getHospitalList } from '@/api/hospital'
import { getDrugList } from '@/api/drug'
import { ElMessage, ElMessageBox } from 'element-plus'

const statusTagMap = {
  PENDING: { label: '待审批', type: 'warning' },
  APPROVED: { label: '已审批', type: '' },
  DELIVERED: { label: '已发货', type: 'info' },
  COMPLETED: { label: '已完成', type: 'success' }
}

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const hospitalList = ref([])
const drugList = ref([])

const query = reactive({ orderNo: '', hospitalId: '', status: '', page: 1, size: 10 })

const createVisible = ref(false)
const submitLoading = ref(false)
const createFormRef = ref(null)

const createForm = reactive({
  hospitalId: null,
  supplier: '',
  remark: '',
  items: [{ drugId: null, quantity: 1, unitPrice: 0 }]
})

const createRules = {
  hospitalId: [{ required: true, message: '请选择采购机构', trigger: 'change' }],
  supplier: [{ required: true, message: '请输入供应商', trigger: 'blur' }]
}

const detailVisible = ref(false)
const detail = reactive({
  orderNo: '', hospitalName: '', supplier: '', status: '', totalAmount: 0,
  createdAt: '', remark: '', items: []
})

onMounted(() => {
  fetchData()
  fetchHospitalList()
  fetchDrugList()
})

async function fetchData() {
  loading.value = true
  try {
    const params = { page: query.page, size: query.size }
    if (query.orderNo) params.orderNo = query.orderNo
    if (query.hospitalId) params.hospitalId = query.hospitalId
    if (query.status) params.status = query.status
    const res = await getPurchaseOrderPage(params)
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
  query.orderNo = ''
  query.hospitalId = ''
  query.status = ''
  query.page = 1
  fetchData()
}

function openCreateDialog() {
  createForm.hospitalId = null
  createForm.supplier = ''
  createForm.remark = ''
  createForm.items = [{ drugId: null, quantity: 1, unitPrice: 0 }]
  createVisible.value = true
}

function addItem() {
  createForm.items.push({ drugId: null, quantity: 1, unitPrice: 0 })
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
  const hasEmpty = createForm.items.some(item => !item.drugId)
  if (hasEmpty) {
    ElMessage.warning('请选择所有药品')
    return
  }
  submitLoading.value = true
  try {
    await createPurchaseOrder({
      hospitalId: createForm.hospitalId,
      supplier: createForm.supplier,
      remark: createForm.remark,
      items: createForm.items
    })
    ElMessage.success('采购单创建成功')
    createVisible.value = false
    fetchData()
  } finally {
    submitLoading.value = false
  }
}

async function openDetailDialog(row) {
  try {
    const res = await getPurchaseOrder(row.id)
    Object.assign(detail, res.data)
    detailVisible.value = true
  } catch {
    /* handled */
  }
}

async function handleApprove(row) {
  try {
    await ElMessageBox.confirm(`确定审批通过订单「${row.orderNo}」吗？`, '审批确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    await approvePurchaseOrder(row.id)
    ElMessage.success('审批通过')
    fetchData()
  } catch {
    /* cancelled */
  }
}

async function handleReceive(row) {
  try {
    await ElMessageBox.confirm(`确定确认收货订单「${row.orderNo}」吗？`, '收货确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    await receivePurchaseOrder(row.id)
    ElMessage.success('收货成功')
    fetchData()
  } catch {
    /* cancelled */
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除采购单「${row.orderNo}」吗？`, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deletePurchaseOrder(row.id)
    ElMessage.success('删除成功')
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
