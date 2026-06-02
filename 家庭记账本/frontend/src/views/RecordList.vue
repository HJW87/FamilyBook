<template>
  <div class="page-container">
    <div class="list-card">
      <!-- 头部 -->
      <div class="list-header">
        <div>
          <h2 class="page-title">📋 收支明细</h2>
          <p class="page-subtitle">家庭所有成员的收支记录一览</p>
        </div>
        <el-button type="primary" size="large" round @click="$router.push('/record-add')">
          ✏️ 记一笔
        </el-button>
      </div>

      <!-- 筛选栏 -->
      <div class="filter-bar">
        <el-select v-model="filters.type" placeholder="全部类型" clearable style="width:110px" @change="onFilterChange">
          <el-option label="📤 支出" value="EXPENSE" />
          <el-option label="📥 收入" value="INCOME" />
        </el-select>
        <el-select v-model="filters.categoryId" placeholder="全部类别" clearable style="width:150px" @change="onFilterChange">
          <el-option v-for="c in allCategories" :key="c.id" :label="c.icon + ' ' + c.name" :value="c.id" />
        </el-select>
        <el-select v-model="filters.familyMember" placeholder="全部记录人" clearable style="width:120px" @change="onFilterChange">
          <el-option v-for="m in members" :key="m.id" :label="'👤 ' + m.name" :value="m.name" />
        </el-select>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="~"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          style="width:240px"
          @change="onDateRangeChange"
        />
        <el-input v-model="filters.keyword" placeholder="搜索备注..." clearable style="width:160px" @input="onKeywordInput">
          <template #prefix>🔍</template>
        </el-input>
        <el-button @click="resetFilters" text>🔄 重置</el-button>
      </div>

      <!-- 汇总条 -->
      <div class="summary-bar">
        <div class="summary-item">
          <span class="summary-label">共 <b>{{ total }}</b> 笔</span>
        </div>
        <div class="summary-divider"></div>
        <div class="summary-item income">
          <span class="summary-dot"></span>
          <span>收入 <b>¥{{ fmt(summaryIncome) }}</b></span>
        </div>
        <div class="summary-divider"></div>
        <div class="summary-item expense">
          <span class="summary-dot"></span>
          <span>支出 <b>¥{{ fmt(summaryExpense) }}</b></span>
        </div>
      </div>

      <!-- 记录表格 -->
      <el-table :data="records" stripe v-loading="loading" empty-text="暂无记录，去记一笔吧~" style="width:100%"
        :header-cell-style="{ background: '#fafaf8', color: '#666', fontWeight: 600, fontSize: '13px' }">
        <el-table-column prop="recordDate" label="日期" width="115" sortable>
          <template #default="{ row }">
            <span class="date-cell">{{ row.recordDate }}</span>
          </template>
        </el-table-column>
        <el-table-column label="类别" width="120">
          <template #default="{ row }">
            <span class="category-cell">
              <span class="cat-icon">{{ row.categoryIcon }}</span>
              {{ row.categoryName }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="130" sortable prop="amount">
          <template #default="{ row }">
            <span :class="['amount-cell', row.type === 'INCOME' ? 'amount-income' : 'amount-expense']">
              {{ row.type === 'INCOME' ? '+' : '−' }}¥{{ fmt(row.amount) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="记录人" width="90">
          <template #default="{ row }">
            <span class="recorder-tag">{{ row.familyMember || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="note" label="备注" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">
            <span :class="{ 'note-empty': !row.note }">{{ row.note || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <template v-if="canModify(row.userId)">
              <el-button type="primary" link size="small" @click="openEdit(row)">编辑</el-button>
              <el-popconfirm title="确定要删除这条记录吗？" @confirm="handleDelete(row.id)">
                <template #reference>
                  <el-button type="danger" link size="small">删除</el-button>
                </template>
              </el-popconfirm>
            </template>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="filters.page"
          v-model:page-size="filters.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          small
          @change="loadRecords"
        />
      </div>
    </div>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="editVisible" title="编辑记录" width="440px" destroy-on-close center>
      <el-form :model="editForm" label-width="70px" class="edit-form">
        <el-form-item label="类型">
          <el-radio-group v-model="editForm.type" size="large">
            <el-radio-button value="EXPENSE">📤 支出</el-radio-button>
            <el-radio-button value="INCOME">📥 收入</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="金额">
          <el-input v-model="editForm.amount" size="large">
            <template #prepend>¥</template>
          </el-input>
        </el-form-item>
        <el-form-item label="类别">
          <el-select v-model="editForm.categoryId" style="width:100%" size="large">
            <el-option v-for="c in editCategories" :key="c.id" :label="c.icon + ' ' + c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker v-model="editForm.recordDate" type="date" style="width:100%" size="large"
            format="YYYY-MM-DD" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="editForm.note" maxlength="200" size="large" placeholder="备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false" round>取消</el-button>
        <el-button type="primary" :loading="editSaving" @click="handleUpdate" round>
          💾 保存修改
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getRecords, updateRecord, deleteRecord } from '../api/record'
import { getCategories } from '../api/category'
import { getMembers } from '../api/member'

const loading = ref(false)
const records = ref([])
const total = ref(0)
const summaryIncome = ref(0)
const summaryExpense = ref(0)
const allCategories = ref([])
const members = ref([])
const currentUserId = ref(Number(localStorage.getItem('userId')) || null)

const canModify = (recordUserId) => {
  if (!currentUserId.value) return false
  if (!recordUserId) return true
  return recordUserId === currentUserId.value
}

const filters = reactive({
  page: 1,
  size: 20,
  type: '',
  categoryId: null,
  familyMember: '',
  keyword: '',
  startDate: '',
  endDate: ''
})

const dateRange = ref([])
let keywordTimer = null

const loadRecords = async () => {
  loading.value = true
  try {
    const params = { ...filters }
    if (!params.type) delete params.type
    if (!params.categoryId) delete params.categoryId
    if (!params.familyMember) delete params.familyMember
    if (!params.keyword) delete params.keyword
    if (!params.startDate) delete params.startDate
    if (!params.endDate) delete params.endDate

    const res = await getRecords(params)
    records.value = res.data.records
    total.value = res.data.total

    let income = 0, expense = 0
    records.value.forEach(r => {
      const amt = parseFloat(r.amount)
      if (r.type === 'INCOME') income += amt
      else expense += amt
    })
    summaryIncome.value = income
    summaryExpense.value = expense
  } catch (e) {
    // handled
  } finally {
    loading.value = false
  }
}

const onFilterChange = () => {
  filters.page = 1
  loadRecords()
}

const onDateRangeChange = (val) => {
  if (val && val.length === 2) {
    filters.startDate = val[0]
    filters.endDate = val[1]
  } else {
    filters.startDate = ''
    filters.endDate = ''
  }
  filters.page = 1
  loadRecords()
}

const onKeywordInput = () => {
  clearTimeout(keywordTimer)
  keywordTimer = setTimeout(() => {
    filters.page = 1
    loadRecords()
  }, 300)
}

const resetFilters = () => {
  filters.type = ''
  filters.categoryId = null
  filters.familyMember = ''
  filters.keyword = ''
  filters.startDate = ''
  filters.endDate = ''
  filters.page = 1
  dateRange.value = []
  loadRecords()
}

// 编辑功能
const editVisible = ref(false)
const editSaving = ref(false)
const editingId = ref(null)
const editForm = reactive({
  type: 'EXPENSE',
  amount: '',
  categoryId: null,
  recordDate: '',
  familyMember: '',
  note: ''
})

const editCategories = computed(() => {
  return allCategories.value.filter(c => c.type === editForm.type)
})

const openEdit = (row) => {
  editingId.value = row.id
  editForm.type = row.type
  editForm.amount = String(row.amount)
  editForm.categoryId = row.categoryId
  editForm.recordDate = row.recordDate
  editForm.familyMember = row.familyMember
  editForm.note = row.note || ''
  editVisible.value = true
}

const handleUpdate = async () => {
  editSaving.value = true
  try {
    await updateRecord(editingId.value, {
      type: editForm.type,
      amount: parseFloat(editForm.amount),
      categoryId: editForm.categoryId,
      recordDate: editForm.recordDate,
      familyMember: editForm.familyMember,
      note: editForm.note
    })
    ElMessage.success('修改成功')
    editVisible.value = false
    loadRecords()
  } catch (e) { /* handled */ }
  finally { editSaving.value = false }
}

const handleDelete = async (id) => {
  try {
    await deleteRecord(id)
    ElMessage.success('删除成功')
    loadRecords()
  } catch (e) { /* handled */ }
}

const fmt = (val) => {
  return Number(val).toFixed(2)
}

onMounted(async () => {
  try {
    const [catRes, memRes] = await Promise.all([
      getCategories(),
      getMembers()
    ])
    allCategories.value = catRes.data
    members.value = memRes.data || []
  } catch (e) { /* handled */ }
  loadRecords()
})
</script>

<style scoped>
.list-card {
  background: var(--white);
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
  padding: 28px;
}

/* 头部 */
.list-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}
.page-title {
  font-size: 22px;
  font-weight: 700;
  margin: 0 0 4px;
  color: var(--text-primary);
}
.page-subtitle {
  font-size: 13px;
  color: #999;
  margin: 0;
}

/* 筛选栏 */
.filter-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 16px;
  align-items: center;
  padding: 14px 16px;
  background: #fafaf8;
  border-radius: 12px;
  border: 1px solid #f0ede0;
}

/* 汇总条 */
.summary-bar {
  padding: 12px 20px;
  background: linear-gradient(135deg, #FFFDF7, #FFF8E7);
  border-radius: 12px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  border: 1px solid #f5e6c8;
}
.summary-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: var(--text-secondary);
}
.summary-item b {
  color: var(--text-primary);
  font-size: 15px;
}
.summary-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}
.summary-item.income .summary-dot { background: var(--income); }
.summary-item.expense .summary-dot { background: var(--expense); }
.summary-divider {
  width: 1px;
  height: 20px;
  background: #e8dcc8;
}

/* 表格 */
.date-cell {
  font-variant-numeric: tabular-nums;
  color: #555;
}
.category-cell {
  display: flex;
  align-items: center;
  gap: 6px;
}
.cat-icon { font-size: 16px; }
.amount-cell {
  font-weight: 700;
  font-size: 14px;
  font-variant-numeric: tabular-nums;
}
.amount-income { color: var(--income); }
.amount-expense { color: var(--expense); }

.recorder-tag {
  display: inline-block;
  padding: 2px 10px;
  background: #f5f3eb;
  border-radius: 10px;
  font-size: 12px;
  color: #888;
}

.note-empty { color: #ccc; }
.text-muted { color: #ccc; font-size: 12px; }

.pagination-wrap {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 编辑弹窗 */
.edit-form :deep(.el-form-item__label) {
  font-weight: 600;
}

/* 响应式 */
@media (max-width: 768px) {
  .list-card {
    border-radius: 0;
    box-shadow: none;
    padding: 16px;
  }
  .list-header {
    flex-direction: column;
    gap: 12px;
  }
  .filter-bar {
    padding: 10px;
    gap: 8px;
  }
  .summary-bar {
    flex-wrap: wrap;
    gap: 10px;
    padding: 10px 16px;
  }
}
</style>
