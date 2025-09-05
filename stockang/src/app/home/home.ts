import { HttpClient } from '@angular/common/http';
import { Component, ElementRef, Input, OnInit, SimpleChanges, ViewChild } from '@angular/core';
import { ChartConfiguration, ChartOptions, ChartType } from 'chart.js';
import { forkJoin } from 'rxjs';
import { HistoryEntry, Stock } from '../../model/stock.model';
@Component({
  selector: 'app-home',
  templateUrl: './home.html',
  standalone: false,
  styleUrls: ['./home.css'],
})
export class home implements OnInit {

  @ViewChild('chartSection') chartSection!: ElementRef;

  allHistory: HistoryEntry[] = [];
  stocks: Stock[] = [];
  selectedStock: Stock | null = null;
  selectedRange: number = 30;

  chartData: ChartConfiguration<'line'>['data'] = {
    labels: [],
    datasets: [],
  };

  chartOptions: ChartOptions<'line'> = {
    responsive: true,
    plugins: {
      legend: {
        display: true,
        labels: {
          color: '#fff'
        }
      }
    },
    scales: {
      x: { ticks: { color: '#ccc' } },
      y: { ticks: { color: '#ccc' } }
    }
  };

  constructor(private http: HttpClient) { }

  ngOnInit(): void {
    this.fetchStocksAndHistories();
  }

  fetchStocksAndHistories(): void {
    this.http.get<{ symbol: string; Name: string; basePrice: number; Industry: string }[]>('http://localhost:8089/api/stocks')
      .subscribe(stockList => {
        const historyRequests = stockList.map(stock =>
          this.http.get<HistoryEntry[]>(`http://localhost:8089/api/stock/${stock.symbol}/history`)
        );

        forkJoin(historyRequests).subscribe(historiesArray => {
          this.stocks = stockList.map((stock, idx) => {
            const history = historiesArray[idx].sort((a, b) =>
              new Date(a.tradeDate).getTime() - new Date(b.tradeDate).getTime()
            );

            const currentPrice = history.length ? history[history.length - 1].closePrice : stock.basePrice;
            const change = currentPrice - stock.basePrice;
            const changePercent = stock.basePrice ? (change / stock.basePrice) * 100 : 0;

            return {
              symbol: stock.symbol,
              name: stock.Name,
              basePrice: stock.basePrice,
              currentPrice,
              industry: stock.Industry,
              history,
              change,
              changePercent
            };
          });
        });
      });
  }

  /**
   * Call this when a user clicks on a stock or changes date range
   */
  fetchHistoryForChart(stock: Stock, days: number = this.selectedRange): void {
    this.selectedStock = stock;
    this.selectedRange = days;

    const fullHistory = stock.history;

    // Filter the last N days
    const filteredHistory = fullHistory.slice(-days);

    const labels = filteredHistory.map(entry =>
      new Date(entry.tradeDate).toLocaleDateString()
    );

    const prices = filteredHistory.map(entry => entry.closePrice ?? 0);

    this.chartData = {
      labels: labels,
      datasets: [{
        label: `${stock.symbol} - Last ${days} Days`,
        data: prices,
        fill: false,
        borderColor: '#D65A31',
        backgroundColor: '#D65A31',
        tension: 0.3,
        pointRadius: 4,
        pointHoverRadius: 6
      }]
    };

    // Smooth scroll to chart section
    setTimeout(() => {
      this.chartSection?.nativeElement.scrollIntoView({ behavior: 'smooth' });
    }, 0);
  }

  /**
   * Handler for the range buttons
   */
  setChartRange(days: number): void {
    if (!this.selectedStock) return;
    this.fetchHistoryForChart(this.selectedStock, days);
  }
}
