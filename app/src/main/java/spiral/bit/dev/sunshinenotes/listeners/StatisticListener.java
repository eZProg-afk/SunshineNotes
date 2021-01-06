package spiral.bit.dev.sunshinenotes.listeners;

import spiral.bit.dev.sunshinenotes.models.other.Statistic;

public interface StatisticListener {
    void onStatisticsClicked(Statistic statistic, int position);
    void onLongStatisticsClicked(Statistic statistic, int position);
}
