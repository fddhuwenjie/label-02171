<template>
  <div class="dashboard-page">
    <!-- 统计卡片区域 -->
    <div class="stats-grid">
      <div
        v-for="item in statCards"
        :key="item.key"
        class="stat-card"
        :class="{ clickable: item.route, 'alert-card': item.alert }"
        @click="item.route && $router.push(item.route)"
      >
        <div class="stat-icon" :style="{ backgroundColor: item.bgColor }">
          <el-icon :size="26" :color="item.color"><component :is="item.icon" /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value" :style="{ color: item.color }">
            <span v-if="statsLoading">-</span>
            <span v-else>{{ stats[item.key] ?? 0 }}</span>
            <el-tag v-if="item.alert && stats[item.key] > 0" type="danger" size="small" effect="dark" class="alert-badge">
              预警
            </el-tag>
          </div>
          <div class="stat-label">{{ item.label }}</div>
        </div>
      </div>
    </div>

    <!-- 数据面板区域 -->
    <el-row :gutter="16" class="panel-row">
      <!-- 效期预警（红色标签） -->
      <el-col :span="8">
        <div class="data-panel">
          <div class="panel-header">
            <span class="panel-title">
              <el-icon class="title-icon expiry"><AlarmClock /></el-icon>
              效期预警
              <el-tag v-if="expiryAlerts.length" type="danger" size="small" effect="dark" class="panel-count-badge">
                {{ expiryAlerts.length }}
              </el-tag>
            </span>
            <el-button type="primary" link size="small" @click="$router.push('/expiry-alert')">
              查看全部 <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
          <div class="panel-body" v-loading="expiryLoading">
            <template v-if="expiryAlerts.length">
              <div
                v-for="item in expiryAlerts.slice(0, 5)"
                :key="item.id"
                class="list-item expiry-item"
              >
                <div class="item-content">
                  <div class="item-main">
                    <span class="item-name">{{ item.drugName }}</span>
                    <el-tag
                      :type="item.alertLevel === 'RED' ? 'danger' : item.alertLevel === 'EXPIRED' ? 'info' : 'warning'"
                      size="small"
                      effect="dark"
                      class="expiry-tag"
                    >
                      {{ item.alertLevel === 'RED' ? '紧急' : item.alertLevel === 'EXPIRED' ? '已过期' : '预警' }}
                    </el-tag>
                  </div>
                  <div class="item-sub">{{ item.hospitalName }} · 批号: {{ item.batchNo || '-' }}</div>
                </div>
                <div class="item-extra">
                  <span :class="daysClass(item.daysToExpire)">
                    {{ item.daysToExpire >= 0 ? `${item.daysToExpire}天` : `过期${-item.daysToExpire}天` }}
                  </span>
                </div>
              </div>
            </template>
            <el-empty v-else description="暂无近效期预警" :image-size="60" />
          </div>
        </div>
      </el-col>

      <!-- 库存预警 -->
      <el-col :span="8">
        <div class="data-panel">
          <div class="panel-header">
            <span class="panel-title">
              <el-icon class="title-icon warning"><WarningFilled /></el-icon>
              库存预警
            </span>
            <el-button type="primary" link size="small" @click="$router.push('/inventory')">
              查看全部 <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
          <div class="panel-body" v-loading="warningsLoading">
            <template v-if="warnings.length">
              <div
                v-for="item in warnings.slice(0, 5)"
                :key="item.id"
                class="list-item warning-item"
              >
                <div class="item-content">
                  <div class="item-main">
                    <span class="item-name">{{ item.drugName }}</span>
                  </div>
                  <div class="item-sub">{{ item.hospitalName }}</div>
                </div>
                <div class="item-extra">
                  <span class="stock-value danger">{{ item.quantity }}</span>
                  <span class="stock-divider">/</span>
                  <span class="stock-min">{{ item.minStock }}</span>
                </div>
              </div>
            </template>
            <el-empty v-else description="暂无预警" :image-size="60" />
          </div>
        </div>
      </el-col>

      <!-- 待审批调拨 -->
      <el-col :span="8">
        <div class="data-panel">
          <div class="panel-header">
            <span class="panel-title">
              <el-icon class="title-icon transfer"><Switch /></el-icon>
              待审批调拨
            </span>
            <el-button type="primary" link size="small" @click="$router.push('/transfer')">
              查看全部 <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
          <div class="panel-body" v-loading="transfersLoading">
            <template v-if="pendingTransfers.length">
              <div
                v-for="item in pendingTransfers.slice(0, 5)"
                :key="item.id"
                class="list-item"
              >
                <div class="item-content">
                  <div class="item-main">
                    <span class="item-name">{{ item.fromHospitalName }} → {{ item.toHospitalName }}</span>
                  </div>
                  <div class="item-sub" v-if="item.drugName">{{ item.drugName }}</div>
                </div>
                <el-tag class="item-tag" size="small" type="warning">待审批</el-tag>
              </div>
            </template>
            <el-empty v-else description="暂无待审批调拨" :image-size="60" />
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 第二行 -->
    <el-row :gutter="16" class="panel-row">
      <!-- 待审批采购 -->
      <el-col :span="12">
        <div class="data-panel">
          <div class="panel-header">
            <span class="panel-title">
              <el-icon class="title-icon purchase"><ShoppingCart /></el-icon>
              待审批采购
            </span>
            <el-button type="primary" link size="small" @click="$router.push('/purchase')">
              查看全部 <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
          <div class="panel-body" v-loading="purchasesLoading">
            <template v-if="pendingPurchases.length">
              <div
                v-for="item in pendingPurchases.slice(0, 5)"
                :key="item.id"
                class="list-item"
              >
                <div class="item-content">
                  <div class="item-main">
                    <span class="item-name">{{ item.hospitalName }}</span>
                  </div>
                  <div class="item-sub">{{ item.orderNo }} · ¥{{ item.totalAmount }}</div>
                </div>
                <el-tag class="item-tag" size="small" type="warning">待审批</el-tag>
              </div>
            </template>
            <el-empty v-else description="暂无待审批采购" :image-size="60" />
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 快捷操作 -->
    <div class="quick-actions">
      <div class="actions-title">快捷操作</div>
      <div class="actions-grid">
        <div class="action-card" @click="$router.push('/drug')">
          <div class="action-icon" style="background: linear-gradient(135deg, #FF7A45 0%, #FF9A6C 100%)">
            <el-icon :size="24"><Tickets /></el-icon>
          </div>
          <span class="action-text">药品管理</span>
        </div>
        <div class="action-card" @click="$router.push('/inventory')">
          <div class="action-icon" style="background: linear-gradient(135deg, #1890FF 0%, #69C0FF 100%)">
            <el-icon :size="24"><Box /></el-icon>
          </div>
          <span class="action-text">库存管理</span>
        </div>
        <div class="action-card" @click="$router.push('/purchase')">
          <div class="action-icon" style="background: linear-gradient(135deg, #52C41A 0%, #95DE64 100%)">
            <el-icon :size="24"><ShoppingCart /></el-icon>
          </div>
          <span class="action-text">采购管理</span>
        </div>
        <div class="action-card" @click="$router.push('/transfer')">
          <div class="action-icon" style="background: linear-gradient(135deg, #722ED1 0%, #B37FEB 100%)">
            <el-icon :size="24"><Switch /></el-icon>
          </div>
          <span class="action-text">药品调拨</span>
        </div>
        <div class="action-card alert-action" @click="$router.push('/expiry-alert')">
          <div class="action-icon" style="background: linear-gradient(135deg, #F5222D 0%, #FF7875 100%)">
            <el-icon :size="24"><AlarmClock /></el-icon>
          </div>
          <span class="action-text">效期预警</span>
        </div>
        <div class="action-card" @click="$router.push('/hospital')">
          <div class="action-icon" style="background: linear-gradient(135deg, #13C2C2 0%, #5CDBD3 100%)">
            <el-icon :size="24"><OfficeBuilding /></el-icon>
          </div>
          <span class="action-text">机构管理</span>
        </div>
        <div class="action-card" @click="$router.push('/log')">
          <div class="action-icon" style="background: linear-gradient(135deg, #FA8C16 0%, #FFC069 100%)">
            <el-icon :size="24"><Document /></el-icon>
          </div>
          <span class="action-text">库存日志</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getStats } from '@/api/dashboard'
import { getWarnings } from '@/api/inventory'
import { getTransferPage } from '@/api/transfer'
import { getPurchaseOrderPage } from '@/api/purchase'
import { getActiveExpiryAlerts } from '@/api/expiryAlert'
import {
  Tickets, OfficeBuilding, WarningFilled, Switch, ShoppingCart,
  ArrowRight, Box, Document, AlarmClock
} from '@element-plus/icons-vue'

const statsLoading = ref(false)
const warningsLoading = ref(false)
const transfersLoading = ref(false)
const purchasesLoading = ref(false)
const expiryLoading = ref(false)

const stats = reactive({
  drugCount: 0,
  hospitalCount: 0,
  lowStockCount: 0,
  pendingTransferCount: 0,
  pendingPurchaseCount: 0,
  expiryAlertCount: 0
})

const warnings = ref([])
const pendingTransfers = ref([])
const pendingPurchases = ref([])
const expiryAlerts = ref([])

const statCards = [
  { key: 'drugCount', label: '药品总数', icon: Tickets, color: '#FF7A45', bgColor: '#FFF2E8', route: '/drug' },
  { key: 'hospitalCount', label: '机构总数', icon: OfficeBuilding, color: '#1890FF', bgColor: '#E6F7FF', route: '/hospital' },
  { key: 'lowStockCount', label: '库存预警', icon: WarningFilled, color: '#F5222D', bgColor: '#FFF1F0', route: '/inventory', alert: true },
  { key: 'expiryAlertCount', label: '效期预警', icon: AlarmClock, color: '#F5222D', bgColor: '#FFF1F0', route: '/expiry-alert', alert: true },
  { key: 'pendingTransferCount', label: '待审批调拨', icon: Switch, color: '#722ED1', bgColor: '#F9F0FF', route: '/transfer' },
  { key: 'pendingPurchaseCount', label: '待审批采购', icon: ShoppingCart, color: '#52C41A', bgColor: '#F6FFED', route: '/purchase' }
]

function daysClass(days) {
  if (days < 0) return 'days-expired'
  if (days <= 30) return 'days-red'
  return 'days-yellow'
}

onMounted(() => {
  fetchStats()
  fetchWarnings()
  fetchPendingTransfers()
  fetchPendingPurchases()
  fetchExpiryAlerts()
})

async function fetchStats() {
  statsLoading.value = true
  try {
    const res = await getStats()
    Object.assign(stats, res.data)
  } finally {
    statsLoading.value = false
  }
}

async function fetchWarnings() {
  warningsLoading.value = true
  try {
    const res = await getWarnings()
    warnings.value = res.data || []
  } finally {
    warningsLoading.value = false
  }
}

async function fetchPendingTransfers() {
  transfersLoading.value = true
  try {
    const res = await getTransferPage({ status: 'PENDING', pageSize: 5 })
    pendingTransfers.value = res.data?.records || []
  } finally {
    transfersLoading.value = false
  }
}

async function fetchPendingPurchases() {
  purchasesLoading.value = true
  try {
    const res = await getPurchaseOrderPage({ status: 'PENDING', pageSize: 5 })
    pendingPurchases.value = res.data?.records || []
  } finally {
    purchasesLoading.value = false
  }
}

async function fetchExpiryAlerts() {
  expiryLoading.value = true
  try {
    const res = await getActiveExpiryAlerts(5)
    expiryAlerts.value = res.data || []
  } finally {
    expiryLoading.value = false
  }
}
</script>

<style lang="scss" scoped>
.dashboard-page {
  .stats-grid {
    display: grid;
    grid-template-columns: repeat(6, 1fr);
    gap: 16px;
    margin-bottom: 16px;
  }

  .stat-card {
    background: #fff;
    border-radius: 10px;
    padding: 20px 18px;
    display: flex;
    align-items: center;
    gap: 14px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    transition: all 0.2s;
    position: relative;

    &.clickable {
      cursor: pointer;
      &:hover {
        transform: translateY(-3px);
        box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
      }
    }

    &.alert-card {
      border: 1px solid #FFCCC7;
    }

    .stat-icon {
      width: 52px;
      height: 52px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
    }

    .stat-info {
      min-width: 0;
      flex: 1;

      .stat-value {
        font-size: 28px;
        font-weight: 700;
        line-height: 1.2;
        display: flex;
        align-items: center;
        gap: 6px;
      }

      .alert-badge {
        font-size: 11px;
        transform: scale(0.85);
        transform-origin: left center;
      }

      .stat-label {
        font-size: 14px;
        color: #8C8C8C;
        margin-top: 4px;
        white-space: nowrap;
      }
    }
  }

  .panel-row {
    margin-bottom: 16px;
  }

  .data-panel {
    background: #fff;
    border-radius: 10px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    height: 380px;
    display: flex;
    flex-direction: column;

    .panel-header {
      padding: 16px 20px;
      border-bottom: 1px solid #F0F0F0;
      display: flex;
      align-items: center;
      justify-content: space-between;
      flex-shrink: 0;

      .panel-title {
        font-size: 15px;
        font-weight: 600;
        color: #333;
        display: flex;
        align-items: center;
        gap: 8px;

        .title-icon {
          font-size: 18px;
          &.expiry { color: #F5222D; }
          &.warning { color: #F5222D; }
          &.transfer { color: #722ED1; }
          &.purchase { color: #52C41A; }
        }

        .panel-count-badge {
          font-size: 11px;
        }
      }
    }

    .panel-body {
      flex: 1;
      padding: 12px 0;
      overflow-y: auto;

      .list-item {
        padding: 14px 20px;
        border-bottom: 1px solid #FAFAFA;
        transition: background 0.2s;
        display: flex;
        align-items: center;
        justify-content: space-between;

        &:hover {
          background: #FAFAFA;
        }

        &:last-child {
          border-bottom: none;
        }

        &.expiry-item {
          .item-main .item-name {
            color: #F5222D;
          }
        }

        .item-content {
          flex: 1;
          min-width: 0;
          display: flex;
          flex-direction: column;
          justify-content: center;
        }

        .item-main {
          display: flex;
          align-items: center;
          gap: 10px;

          .item-name {
            font-size: 14px;
            color: #333;
            font-weight: 500;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }

          .expiry-tag {
            flex-shrink: 0;
          }
        }

        .item-sub {
          font-size: 13px;
          color: #666;
          margin-top: 4px;
          line-height: 1.4;
        }

        .item-tag {
          flex-shrink: 0;
          margin-left: 12px;
        }

        .item-extra {
          display: flex;
          align-items: center;
          gap: 2px;
          font-size: 14px;
          font-weight: 700;
          flex-shrink: 0;
          margin-left: 12px;

          .days-red { color: #F5222D; }
          .days-yellow { color: #FAAD14; }
          .days-expired { color: #8C8C8C; }

          .stock-value {
            font-weight: 600;
            &.danger { color: #F5222D; }
          }
          .stock-divider {
            color: #D9D9D9;
          }
          .stock-min {
            color: #999;
            font-weight: 400;
          }
        }
      }

      :deep(.el-empty) {
        padding: 50px 0;
        .el-empty__description {
          margin-top: 12px;
        }
      }
    }
  }

  .quick-actions {
    background: #fff;
    border-radius: 10px;
    padding: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

    .actions-title {
      font-size: 15px;
      font-weight: 600;
      color: #333;
      margin-bottom: 16px;
    }

    .actions-grid {
      display: grid;
      grid-template-columns: repeat(7, 1fr);
      gap: 16px;
    }

    .action-card {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 10px;
      padding: 20px 16px;
      border-radius: 10px;
      cursor: pointer;
      transition: all 0.2s;
      background: #FAFAFA;

      &:hover {
        background: #F0F0F0;
        transform: translateY(-3px);
      }

      &.alert-action {
        background: #FFF1F0;
        &:hover { background: #FFCCC7; }
      }

      .action-icon {
        width: 48px;
        height: 48px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #fff;
      }

      .action-text {
        font-size: 14px;
        color: #666;
        font-weight: 500;
      }
    }
  }
}
</style>
