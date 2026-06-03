<template>
  <div class="page-container">
    <h2 class="page-title">📊 统计分析</h2>

    <!-- 时间范围 + 查看范围 -->
    <div class="period-bar">
      <el-radio-group v-model="period" @change="loadAll">
        <el-radio-button value="year">本年</el-radio-button>
        <el-radio-button value="month">本月</el-radio-button>
        <el-radio-button value="week">本周</el-radio-button>
        <el-radio-button value="day">今日</el-radio-button>
        <el-radio-button value="custom">自定义</el-radio-button>
      </el-radio-group>
      <el-date-picker
        v-if="period === 'custom'"
        v-model="customRange"
        type="daterange"
        range-separator="~"
        start-placeholder="开始"
        end-placeholder="结束"
        format="YYYY-MM-DD"
        value-format="YYYY-MM-DD"
        style="margin-left:12px;width:260px"
        @change="loadAll"
      />
      <div class="view-scope">
        <span class="scope-label">查看范围：</span>
        <el-select v-model="viewUserId" @change="loadAll" style="width:160px">
          <el-option :value="null" label="👨‍👩‍👧‍👦 家庭总账单" />
          <el-option
            v-for="m in memberOptions"
            :key="m.id"
            :value="m.userId"
            :label="'👤 ' + m.name + (m.userId === currentUserId ? '（我）' : '')"
            :disabled="!m.userId"
          />
        </el-select>
      </div>
    </div>

    <!-- 汇总卡片 -->
    <SummaryCards :summary="summary" />

    <!-- 图表区 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :xs="24" :lg="12">
        <div class="chart-card">
          <h3 class="chart-title">按类别{{ categoryType === 'EXPENSE' ? '支出' : '收入' }}分布</h3>
          <el-radio-group v-model="categoryType" size="small" @change="loadByCategory" style="margin-bottom:12px">
            <el-radio-button value="EXPENSE">支出</el-radio-button>
            <el-radio-button value="INCOME">收入</el-radio-button>
          </el-radio-group>
          <div ref="pieChartRef" class="chart-box"></div>
        </div>
      </el-col>
      <el-col :xs="24" :lg="12">
        <div class="chart-card">
          <h3 class="chart-title">按家庭成员对比</h3>
          <div ref="barChartRef" class="chart-box"></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :span="24">
        <div class="chart-card">
          <h3 class="chart-title">月度收支趋势（近12个月）</h3>
          <div ref="lineChartRef" class="chart-box" style="height:350px"></div>
        </div>
      </el-col>
    </el-row>

    <!-- 详细数据表格 -->
    <div class="chart-card" style="margin-top:16px">
      <h3 class="chart-title">📋 类别统计明细</h3>
      <el-table :data="categoryStats" stripe empty-text="暂无数据" style="width:100%">
        <el-table-column label="类别" min-width="150">
          <template #default="{ row }">{{ row.icon }} {{ row.categoryName }}</template>
        </el-table-column>
        <el-table-column prop="count" label="笔数" width="100" />
        <el-table-column label="金额" width="150">
          <template #default="{ row }">¥{{ fmt(row.total) }}</template>
        </el-table-column>
        <el-table-column label="占比" width="120">
          <template #default="{ row }">{{ fmt(row.percentage) }}%</template>
        </el-table-column>
        <el-table-column label="占比图" min-width="200">
          <template #default="{ row }">
            <div class="percent-bar">
              <div class="percent-fill" :style="{ width: row.percentage + '%' }"></div>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick, watch } from 'vue'
import * as echarts from 'echarts'
import { getSummary, getByCategory, getByMember, getMonthlyTrend } from '../api/stats'
import { getMembers } from '../api/member'
import SummaryCards from '../components/SummaryCards.vue'

const period = ref('month')
const customRange = ref([])
const categoryType = ref('EXPENSE')
const viewUserId = ref(null)
const memberOptions = ref([])
const currentUserId = ref(Number(localStorage.getItem('userId')) || null)
const summary = reactive({ totalIncome: 0, totalExpense: 0, balance: 0, recordCount: 0, periodLabel: '' })
const categoryStats = ref([])

// ECharts 引用
const pieChartRef = ref(null)
const barChartRef = ref(null)
const lineChartRef = ref(null)
let pieChart = null
let barChart = null
let lineChart = null

const COLORS = ['#FFA500', '#FFB733', '#FFC966', '#FFDB99', '#FFEDCC', '#E8960A', '#CC7A00', '#B36B00', '#995C00', '#804D00']

const getDateParams = () => {
  const params = { period: period.value }
  if (period.value === 'custom' && customRange.value?.length === 2) {
    params.startDate = customRange.value[0]
    params.endDate = customRange.value[1]
    params.period = 'custom'
  }
  if (viewUserId.value) {
    params.userId = viewUserId.value
  }
  return params
}

// 加载汇总
const loadSummary = async () => {
  try {
    const res = await getSummary(getDateParams())
    Object.assign(summary, res.data)
  } catch (e) { /* handled */ }
}

// 加载按类别统计（饼图）
const loadByCategory = async () => {
  try {
    const params = { ...getDateParams(), type: categoryType.value }
    const res = await getByCategory(params)
    categoryStats.value = res.data
    renderPieChart(res.data)
  } catch (e) { /* handled */ }
}

// 加载按成员统计（柱状图）
const loadByMember = async () => {
  try {
    const res = await getByMember(getDateParams())
    renderBarChart(res.data)
  } catch (e) { /* handled */ }
}

// 加载月度趋势（折线图）
const loadMonthlyTrend = async () => {
  try {
    const res = await getMonthlyTrend({ months: 12 })
    renderLineChart(res.data)
  } catch (e) { /* handled */ }
}

// 渲染饼图
const renderPieChart = (data) => {
  if (!pieChartRef.value) return
  if (!pieChart) pieChart = echarts.init(pieChartRef.value)

  const chartData = data.map((d, i) => ({
    name: d.icon + ' ' + d.categoryName,
    value: parseFloat(d.total)
  }))

  pieChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: ¥{c} ({d}%)' },
    series: [{
      type: 'pie',
      radius: ['45%', '75%'],
      center: ['50%', '55%'],
      data: chartData,
      label: { formatter: '{b} {d}%', fontSize: 12 },
      itemStyle: { borderRadius: 4, borderColor: '#fff', borderWidth: 2 },
      color: COLORS
    }]
  })
}

// 渲染柱状图
const renderBarChart = (data) => {
  if (!barChartRef.value) return
  if (!barChart) barChart = echarts.init(barChartRef.value)

  const members = data.map(d => d.familyMember)
  const incomes = data.map(d => parseFloat(d.income))
  const expenses = data.map(d => parseFloat(d.expense))

  barChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['收入', '支出'], bottom: 0 },
    xAxis: { type: 'category', data: members },
    yAxis: { type: 'value' },
    series: [
      { name: '收入', type: 'bar', data: incomes, itemStyle: { color: '#27AE60', borderRadius: [4,4,0,0] }, barWidth: '40%' },
      { name: '支出', type: 'bar', data: expenses, itemStyle: { color: '#E74C3C', borderRadius: [4,4,0,0] }, barWidth: '40%' }
    ],
    grid: { left: '3%', right: '4%', bottom: '10%', containLabel: true }
  })
}

// 渲染折线图
const renderLineChart = (data) => {
  if (!lineChartRef.value) return
  if (!lineChart) lineChart = echarts.init(lineChartRef.value)

  const months = data.map(d => d.month)
  const incomes = data.map(d => parseFloat(d.income))
  const expenses = data.map(d => parseFloat(d.expense))

  lineChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['收入', '支出'], bottom: 0 },
    xAxis: { type: 'category', data: months, axisLabel: { rotate: 45 } },
    yAxis: { type: 'value' },
    series: [
      { name: '收入', type: 'line', data: incomes, smooth: true, lineStyle: { color: '#27AE60', width: 2 }, itemStyle: { color: '#27AE60' }, areaStyle: { color: 'rgba(39,174,96,0.1)' } },
      { name: '支出', type: 'line', data: expenses, smooth: true, lineStyle: { color: '#E74C3C', width: 2 }, itemStyle: { color: '#E74C3C' }, areaStyle: { color: 'rgba(231,76,60,0.1)' } }
    ],
    grid: { left: '3%', right: '4%', bottom: '12%', containLabel: true }
  })
}

// 窗口 resize
const handleResize = () => {
  pieChart?.resize()
  barChart?.resize()
  lineChart?.resize()
}

const loadAll = () => {
  loadSummary()
  loadByCategory()
  loadByMember()
  loadMonthlyTrend()
}

const fmt = (val) => {
  return Number(val || 0).toFixed(2)
}

onMounted(async () => {
  try {
    const res = await getMembers()
    memberOptions.value = res.data || []
  } catch (e) { /* handled */ }
  loadAll()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  pieChart?.dispose()
  barChart?.dispose()
  lineChart?.dispose()
})
</script>

<style scoped>
.page-title {
  font-size: 22px;
  font-weight: 700;
  margin-bottom: 20px;
}

.period-bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 24px;
  padding: 8px;
  background: var(--white);
  border-radius: 14px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-light);
}

.period-bar :deep(.el-radio-group) {
  box-shadow: none;
}

.period-bar :deep(.el-radio-button__inner) {
  border: none !important;
  background: transparent;
  padding: 8px 18px;
  font-weight: 500;
}

.period-bar :deep(.el-radio-button.is-active .el-radio-button__inner) {
  background: var(--accent);
  color: white;
  border-radius: 10px;
  box-shadow: 0 2px 6px rgba(255,165,0,0.25);
}

.view-scope {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
}

.scope-label {
  font-size: 13px;
  color: var(--text-secondary);
  white-space: nowrap;
  font-weight: 500;
}

.chart-row {
  margin-bottom: 16px;
}

.chart-card {
  background: var(--white);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow);
  padding: 24px;
  margin-bottom: 16px;
  border: 1px solid var(--border-light);
}

.chart-title {
  font-size: 16px;
  font-weight: 700;
  margin-bottom: 14px;
  color: var(--text-primary);
}

.chart-box {
  width: 100%;
  height: 320px;
}

.percent-bar {
  height: 14px;
  background: #f5f3eb;
  border-radius: 7px;
  overflow: hidden;
}

.percent-fill {
  height: 100%;
  background: linear-gradient(90deg, #FFB733, #FFA500);
  border-radius: 7px;
  transition: width 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

@media (max-width: 768px) {
  .period-bar {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
    padding: 12px;
  }
  .view-scope {
    margin-left: 0;
  }
  .chart-card {
    padding: 16px;
  }
  .chart-box {
    height: 260px;
  }
}
</style>
