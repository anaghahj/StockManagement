import { Component, signal } from '@angular/core';
import { Stock } from '../model/stock.model';

@Component({
  selector: 'app-root',
  templateUrl: './app.html',
  standalone: false,
  styleUrls: ['./app.css']
})
export class App {
  protected readonly title = signal('stockang');
  selectedStock: Stock | null = null;

  onStockSelected(stock: Stock) {
    this.selectedStock = stock;
  }
}
