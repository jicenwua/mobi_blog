<script setup>
import { computed } from 'vue'
import SectionTitle from '@/components/common/SectionTitle.vue'
import { useLifeCountdown } from '@/composables/useLifeCountdown'

const {
  todayProgress,
  weekProgress,
  monthProgress,
  yearProgress,
  todayElapsedHours,
  weekElapsedDays,
  monthElapsedDays,
  yearElapsedMonths,
} = useLifeCountdown()

const items = computed(() => [
  {
    key: 'today',
    prefix: '今日已经过去',
    unit: '小时',
    color: '#5cadff',
    stripeLight: '#7ec8ff',
    progress: todayProgress.value,
    elapsed: todayElapsedHours.value,
  },
  {
    key: 'week',
    prefix: '这周已经过去',
    unit: '天',
    color: '#f0a020',
    stripeLight: '#f5bc4d',
    progress: weekProgress.value,
    elapsed: weekElapsedDays.value,
  },
  {
    key: 'month',
    prefix: '本月已经过去',
    unit: '天',
    color: '#f56c6c',
    stripeLight: '#f89898',
    progress: monthProgress.value,
    elapsed: monthElapsedDays.value,
  },
  {
    key: 'year',
    prefix: '今年已经过去',
    unit: '个月',
    color: '#67c23a',
    stripeLight: '#85ce61',
    progress: yearProgress.value,
    elapsed: yearElapsedMonths.value,
  },
])
</script>

<template>
  <el-card class="sidebar-card" shadow="never">
    <SectionTitle title="人生倒计时" />
    <div class="countdown-list">
      <div v-for="item in items" :key="item.key" class="countdown-item">
        <p class="countdown-item__desc">
          {{ item.prefix }}
          <strong class="countdown-item__num" :style="{ color: item.color }">
            {{ item.elapsed }}
          </strong>
          {{ item.unit }}
        </p>
        <div class="countdown-item__bar-row">
          <div class="countdown-item__track">
            <div
              class="countdown-item__fill"
              :style="{
                width: `${item.progress}%`,
                '--bar-color': item.color,
                '--bar-stripe-light': item.stripeLight,
              }"
            />
          </div>
          <span class="countdown-item__percent">{{ item.progress }}%</span>
        </div>
      </div>
    </div>
  </el-card>
</template>

<style scoped>
.sidebar-card {
  border: none;
  border-radius: var(--blog-radius);
  margin-bottom: 16px;
}

.sidebar-card :deep(.section-title) {
  margin-bottom: 12px;
}

.countdown-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.countdown-item__desc {
  margin: 0 0 8px;
  font-size: 13px;
  color: var(--blog-text-secondary);
  line-height: 1.6;
}

.countdown-item__num {
  font-weight: 700;
  margin: 0 2px;
}

.countdown-item__bar-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.countdown-item__track {
  flex: 1;
  height: 8px;
  border-radius: 999px;
  background: #f0f0f0;
  overflow: hidden;
}

.countdown-item__fill {
  height: 100%;
  border-radius: 999px;
  transition: width 1s linear;
  background: repeating-linear-gradient(
    -45deg,
    var(--bar-color) 0,
    var(--bar-color) 4px,
    var(--bar-stripe-light) 4px,
    var(--bar-stripe-light) 8px
  );
  background-size: 11px 11px;
  animation: countdown-stripe-flow 0.9s linear infinite;
}

@keyframes countdown-stripe-flow {
  from {
    background-position: 0 0;
  }
  to {
    background-position: 11px 0;
  }
}

.countdown-item__percent {
  flex-shrink: 0;
  min-width: 36px;
  font-size: 13px;
  color: #b0b0b0;
  text-align: right;
}
</style>
