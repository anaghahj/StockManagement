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

  // Full and filtered stock lists
  allStocks: Stock[] = [];
  filteredStocks: Stock[] = [];

  // Filters
  industries: string[] = [];
  searchTerm: string = '';
  selectedIndustry: string = '';
  sortOrder: 'asc' | 'desc' = 'desc';

  // Selected stock & chart
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
        labels: { color: '#fff' }
      }
    },
    scales: {
      x: { ticks: { color: '#ccc' } },
      y: { ticks: { color: '#ccc' } }
    }
  };
  priceOrder: 'high' | 'low' | '' = '';


  constructor(private http: HttpClient) { }

  ngOnInit(): void {
    this.fetchStocksWithHistory();
  }

  /**
   * Fetch stocks + their history to support chart view and filtering
   */
  fetchStocksWithHistory(): void {
    this.http.get<any[]>('http://localhost:8089/api/stocks').subscribe(stockList => {
      const historyCalls = stockList.map(s =>
        this.http.get<HistoryEntry[]>(`http://localhost:8089/api/stock/${s.symbol}/history`)
      );

      forkJoin(historyCalls).subscribe(histories => {
        this.allStocks = stockList.map((s, i) => {
          const h = histories[i].sort((a, b) =>
            new Date(a.tradeDate).getTime() - new Date(b.tradeDate).getTime()
          );

          const cp = h[h.length - 1]?.closePrice ?? s.basePrice;
          const ch = cp - s.basePrice;
          const chp = s.basePrice ? (ch / s.basePrice) * 100 : 0;

          return {
            symbol: s.symbol,
            name: s.name || s.Name,
            basePrice: s.basePrice,
            currentPrice: cp,
            change: ch,
            changePercent: chp,
            industry: s.industry || s.Industry,
            history: h
          };
        });

        this.filteredStocks = [...this.allStocks];
        this.industries = [...new Set(this.allStocks.map(s => s.industry))];
      });
    });
  }

  /**
   * Filters stock list based on search, industry and sort order
   */
  applyFilters(): void {
    this.filteredStocks = this.allStocks
      .filter(stock => {
        const searchMatch = !this.searchTerm ||
          stock.name.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
          stock.symbol.toLowerCase().includes(this.searchTerm.toLowerCase());

        const industryMatch = !this.selectedIndustry || stock.industry === this.selectedIndustry;

        return searchMatch && industryMatch;
      })
      .sort((a, b) => {
        if (this.priceOrder === 'high') return b.currentPrice - a.currentPrice;
        if (this.priceOrder === 'low') return a.currentPrice - b.currentPrice;
        return 0;
      });
  }

  /**
   * Change sorting (triggered via dropdown)
   */
  onSortChange(order: 'asc' | 'desc') {
    this.sortOrder = order;
    this.applyFilters(); // Reapply sorting
  }

  /**
   * When user clicks a stock card
   */
  selectStock(stock: Stock): void {
    this.selectedStock = stock;
    this.renderChart();
  }

  /**
   * Render the line chart based on selected stock + date range
   */
  renderChart(days = this.selectedRange): void {
    if (!this.selectedStock) return;

    const history = this.selectedStock.history.slice(-days);

    this.chartData = {
      labels: history.map(e => new Date(e.tradeDate).toLocaleDateString()),
      datasets: [{
        label: `${this.selectedStock.symbol} — Last ${days} days`,
        data: history.map(e => e.closePrice),
        fill: false,
        borderColor: '#D65A31',
        backgroundColor: '#D65A31',
        tension: 0.3,
        pointRadius: 4,
        pointHoverRadius: 6
      }]
    };

    setTimeout(() => {
      this.chartSection?.nativeElement.scrollIntoView({ behavior: 'smooth' });
    }, 100);
  }

  /**
   * Change date range (e.g. 7, 30, 90 days)
   */
  setRange(days: number): void {
    this.selectedRange = days;
    this.renderChart(days);
  }
}
