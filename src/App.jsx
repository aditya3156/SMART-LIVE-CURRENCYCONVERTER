import { useState, useEffect } from 'react';
import { TrendingUp, ArrowUpDown, Bell, MessageCircle, Send } from 'lucide-react';

const API_URL = 'http://localhost:8080/api';

function App() {
  const [amount, setAmount] = useState(1);
  const [fromCurrency, setFromCurrency] = useState('USD');
  const [toCurrency, setToCurrency] = useState('EUR');
  const [result, setResult] = useState(0);
  const [cryptoRates, setCryptoRates] = useState([]);
  const [news, setNews] = useState([]);
  const [loading, setLoading] = useState(false);
  
  const [chatMessages, setChatMessages] = useState([
    { role: 'bot', content: 'Hi! I can help you with crypto and currency information. Ask me anything!' }
  ]);
  const [chatInput, setChatInput] = useState('');
  const [chatLoading, setChatLoading] = useState(false);

  const currencies = [
    // Americas
    'USD', 'CAD', 'MXN', 'BRL', 'ARS',
    // Europe
    'EUR', 'GBP', 'CHF', 'SEK', 'NOK', 'DKK', 'PLN', 'CZK', 'HUF', 'TRY', 'RUB',
    // Asia Pacific
    'JPY', 'CNY', 'INR', 'HKD', 'SGD', 'KRW', 'AUD', 'NZD', 'THB', 'IDR', 'MYR', 'PHP', 'VND', 'PKR',
    // Middle East & Africa
    'AED', 'SAR', 'ZAR', 'EGP', 'NGN'
  ];

  const cryptoIconMap = {
    'BTC': '₿',
    'ETH': 'Ξ',
    'USDT': '₮',
    'BNB': 'B',
    'XRP': 'X',
    'ADA': '₳',
    'SOL': '◎',
    'DOT': '●',
    'LTC': 'Ł',
  };

  const fetchCryptoPrices = async () => {
    try {
      const response = await fetch(`${API_URL}/crypto/prices?currency=USD`);
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      
      const data = await response.json();
      
      if (!data || typeof data !== 'object' || Object.keys(data).length === 0) {
        console.error('Invalid crypto data:', data);
        setCryptoRates([]);
        return;
      }
      
      const cryptoArray = Object.entries(data).map(([symbol, price]) => ({
        name: symbol === 'BTC' ? 'Bitcoin' : 
              symbol === 'ETH' ? 'Ethereum' : 
              symbol === 'USDT' ? 'Tether' : 
              symbol === 'BNB' ? 'BNB' : 
              symbol === 'XRP' ? 'XRP' : 
              symbol === 'LTC' ? 'Litecoin' : symbol,
        symbol: symbol,
        price: parseFloat(price),
        change: (Math.random() * 10 - 5).toFixed(2),
        icon: cryptoIconMap[symbol] || '◆'
      }));
      
      setCryptoRates(cryptoArray);
      
    } catch (error) {
      console.error('Error fetching crypto:', error);
      setCryptoRates([]);
    }
  };

  const fetchNews = async () => {
    try {
      const response = await fetch(`${API_URL}/news/latest`);
      const data = await response.json();
      
      if (Array.isArray(data)) {
        setNews(data.slice(0, 5));
      } else {
        console.error('Unexpected news format:', data);
        setNews([]);
      }
    } catch (error) {
      console.error('Error fetching news:', error);
      setNews([]);
    }
  };

  const convertCurrency = async () => {
    if (!amount || amount <= 0) {
      setResult(0);
      return;
    }

    if (fromCurrency === toCurrency) {
      setResult(parseFloat(amount).toFixed(2));
      return;
    }

    setLoading(true);
    
    try {
      const response = await fetch(
        `${API_URL}/rates/convert?from=${fromCurrency}&to=${toCurrency}&amount=${amount}`
      );
      const data = await response.json();
      setResult(parseFloat(data.result).toFixed(2));
    } catch (error) {
      console.error('Error converting currency:', error);
      setResult(0);
    } finally {
      setLoading(false);
    }
  };

  const sendChatMessage = async () => {
    if (!chatInput.trim()) return;
    
    const userMessage = { role: 'user', content: chatInput };
    setChatMessages(prev => [...prev, userMessage]);
    setChatInput('');
    setChatLoading(true);
    
    try {
      const response = await fetch(`${API_URL}/chatbot/query`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ query: chatInput })
      });
      
      const data = await response.json();
      const botMessage = { role: 'bot', content: data.message || data.response || 'No response' };
      setChatMessages(prev => [...prev, botMessage]);
    } catch (error) {
      console.error('Chatbot error:', error);
      const errorMessage = { role: 'bot', content: 'Sorry, I encountered an error. Please try again.' };
      setChatMessages(prev => [...prev, errorMessage]);
    } finally {
      setChatLoading(false);
    }
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendChatMessage();
    }
  };

  useEffect(() => {
    fetchCryptoPrices();
    fetchNews();

    const interval = setInterval(() => {
      fetchCryptoPrices();
      fetchNews();
    }, 30000);

    return () => clearInterval(interval);
  }, []);

  useEffect(() => {
    convertCurrency();
  }, [amount, fromCurrency, toCurrency]);

  const swapCurrencies = () => {
    setFromCurrency(toCurrency);
    setToCurrency(fromCurrency);
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-900 via-gray-800 to-gray-900 text-white">
      <header className="border-b border-gray-700 bg-gray-900/80 backdrop-blur-md sticky top-0 z-40">
        <div className="max-w-[1600px] mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="bg-gradient-to-br from-blue-500 to-blue-600 p-2.5 rounded-xl shadow-lg">
                <TrendingUp className="w-6 h-6" />
              </div>
              <div>
                <h1 className="text-2xl font-bold bg-gradient-to-r from-blue-400 to-blue-600 bg-clip-text text-transparent">
                  Smart Live Currency Converter
                </h1>
                <p className="text-xs text-gray-400">Real-time crypto & currency tracking</p>
              </div>
            </div>
            <button className="relative p-2.5 hover:bg-gray-800 rounded-xl transition-all duration-200 group">
              <Bell className="w-5 h-5 group-hover:animate-pulse" />
              <span className="absolute top-1.5 right-1.5 w-2 h-2 bg-green-500 rounded-full animate-pulse"></span>
            </button>
          </div>
        </div>
      </header>

      <main className="max-w-[1600px] mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="grid grid-cols-1 xl:grid-cols-4 gap-6">
          
          {/* Chatbot Assistant */}
          <div className="xl:col-span-1 bg-gradient-to-br from-gray-800/70 to-gray-800/50 backdrop-blur-sm border border-gray-700/50 rounded-2xl shadow-xl flex flex-col h-[600px]">
            <div className="p-5 border-b border-gray-700/50">
              <div className="flex items-center gap-3">
                <div className="bg-gradient-to-br from-purple-500 to-purple-600 p-2.5 rounded-xl shadow-lg">
                  <MessageCircle className="w-5 h-5" />
                </div>
                <div>
                  <h2 className="text-lg font-semibold bg-gradient-to-r from-purple-400 to-purple-600 bg-clip-text text-transparent">
                    Chatbot Assistant
                  </h2>
                  <p className="text-xs text-green-400">● Online</p>
                </div>
              </div>
            </div>

            <div className="flex-1 overflow-y-auto p-4 space-y-3">
              {chatMessages.map((msg, index) => (
                <div key={index} className={`flex ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}>
                  <div className={`max-w-[85%] rounded-xl p-3 shadow-lg text-sm ${
                    msg.role === 'user' 
                      ? 'bg-gradient-to-r from-purple-600 to-purple-700 text-white' 
                      : 'bg-gray-700/50 text-gray-100 border border-gray-600/30'
                  }`}>
                    <p className="whitespace-pre-wrap">{msg.content}</p>
                  </div>
                </div>
              ))}
              {chatLoading && (
                <div className="flex justify-start">
                  <div className="bg-gray-700/50 rounded-xl p-3 border border-gray-600/30">
                    <div className="flex space-x-2">
                      <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce"></div>
                      <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{animationDelay: '0.1s'}}></div>
                      <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{animationDelay: '0.2s'}}></div>
                    </div>
                  </div>
                </div>
              )}
            </div>

            <div className="p-4 border-t border-gray-700/50">
              <div className="flex gap-2">
                <input
                  type="text"
                  value={chatInput}
                  onChange={(e) => setChatInput(e.target.value)}
                  onKeyPress={handleKeyPress}
                  placeholder="Ask me anything..."
                  className="flex-1 bg-gray-700/50 border border-gray-600 rounded-xl px-3 py-2.5 text-sm text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all duration-200"
                  disabled={chatLoading}
                />
                <button
                  onClick={sendChatMessage}
                  disabled={chatLoading || !chatInput.trim()}
                  className="bg-gradient-to-r from-purple-600 to-purple-700 hover:from-purple-500 hover:to-purple-600 disabled:from-gray-600 disabled:to-gray-700 disabled:cursor-not-allowed text-white p-2.5 rounded-xl transition-all duration-200 transform hover:scale-105 active:scale-95"
                >
                  <Send className="w-4 h-4" />
                </button>
              </div>
            </div>
          </div>

          {/* Right Side - 3 Columns */}
          <div className="xl:col-span-3 grid grid-cols-1 lg:grid-cols-3 gap-6">
            
            {/* Currency Converter */}
            <div className="bg-gradient-to-br from-gray-800/70 to-gray-800/50 backdrop-blur-sm border border-gray-700/50 rounded-2xl p-6 shadow-xl hover:shadow-2xl transition-all duration-300 hover:scale-[1.02]">
              <h2 className="text-xl font-semibold bg-gradient-to-r from-blue-400 to-blue-600 bg-clip-text text-transparent mb-6">
                Currency Converter
              </h2>
              
              <div className="space-y-4">
                <div>
                  <label className="block text-sm text-gray-400 mb-2 font-medium">Amount</label>
                  <input
                    type="number"
                    value={amount}
                    onChange={(e) => setAmount(e.target.value)}
                    className="w-full bg-gray-700/50 border border-gray-600 rounded-xl px-4 py-3.5 text-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200"
                    placeholder="Enter amount"
                  />
                </div>

                <div className="grid grid-cols-[1fr,auto,1fr] gap-3 items-end">
                  <div>
                    <label className="block text-sm text-gray-400 mb-2 font-medium">From</label>
                    <select
                      value={fromCurrency}
                      onChange={(e) => setFromCurrency(e.target.value)}
                      className="w-full bg-gray-700/50 border border-gray-600 rounded-xl px-4 py-3.5 text-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200"
                    >
                      {currencies.map(curr => (
                        <option key={curr} value={curr}>
                          {curr}
                        </option>
                      ))}
                    </select>
                  </div>

                  <button 
                    onClick={swapCurrencies} 
                    className="mb-1 p-3 bg-gradient-to-r from-blue-600 to-blue-700 hover:from-blue-500 hover:to-blue-600 rounded-xl transition-all duration-200 transform hover:scale-110 active:scale-95"
                  >
                    <ArrowUpDown className="w-5 h-5" />
                  </button>

                  <div>
                    <label className="block text-sm text-gray-400 mb-2 font-medium">To</label>
                    <select
                      value={toCurrency}
                      onChange={(e) => setToCurrency(e.target.value)}
                      className="w-full bg-gray-700/50 border border-gray-600 rounded-xl px-4 py-3.5 text-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200"
                    >
                      {currencies.map(curr => (
                        <option key={curr} value={curr}>
                          {curr}
                        </option>
                      ))}
                    </select>
                  </div>
                </div>

                <div className="bg-gradient-to-r from-blue-600 to-blue-700 rounded-2xl p-6 mt-6 shadow-lg">
                  <p className="text-sm text-blue-100 mb-1 font-medium">Result</p>
                  <p className="text-3xl font-bold text-white">
                    {loading ? (
                      <span className="animate-pulse">...</span>
                    ) : (
                      `${result} ${toCurrency}`
                    )}
                  </p>
                </div>
              </div>
            </div>

            {/* Live Crypto Rates */}
            <div className="bg-gradient-to-br from-gray-800/70 to-gray-800/50 backdrop-blur-sm border border-gray-700/50 rounded-2xl p-6 shadow-xl hover:shadow-2xl transition-all duration-300">
              <h2 className="text-xl font-semibold bg-gradient-to-r from-orange-400 to-orange-600 bg-clip-text text-transparent mb-6">
                Live Crypto Rates
              </h2>
              
              <div className="space-y-3">
                {cryptoRates.length > 0 ? (
                  cryptoRates.map((crypto) => (
                    <div 
                      key={crypto.symbol} 
                      className="bg-gray-700/30 rounded-xl p-4 hover:bg-gray-700/50 transition-all duration-200 border border-gray-600/30 hover:border-orange-500/30"
                    >
                      <div className="flex items-center justify-between">
                        <div className="flex items-center gap-3">
                          <div className="bg-gradient-to-br from-orange-500 to-orange-600 w-11 h-11 rounded-full flex items-center justify-center text-xl font-bold shadow-lg">
                            {crypto.icon}
                          </div>
                          <div>
                            <p className="font-semibold text-white">{crypto.name}</p>
                            <p className="text-sm text-gray-400">{crypto.symbol}</p>
                          </div>
                        </div>
                        <div className="text-right">
                          <p className="font-semibold text-lg text-white">${crypto.price.toLocaleString()}</p>
                          <p className={`text-sm font-medium ${crypto.change >= 0 ? 'text-green-400' : 'text-red-400'}`}>
                            {crypto.change >= 0 ? '+' : ''}{crypto.change}%
                          </p>
                        </div>
                      </div>
                    </div>
                  ))
                ) : (
                  <p className="text-center text-gray-400 py-8">Loading crypto prices...</p>
                )}
              </div>
            </div>

            {/* Forex News */}
            <div className="bg-gradient-to-br from-gray-800/70 to-gray-800/50 backdrop-blur-sm border border-gray-700/50 rounded-2xl p-6 shadow-xl hover:shadow-2xl transition-all duration-300">
              <h2 className="text-xl font-semibold bg-gradient-to-r from-green-400 to-green-600 bg-clip-text text-transparent mb-6">
                Forex News
              </h2>
              
              <div className="space-y-3">
                {news.length > 0 ? (
                  news.map((item, index) => (
                    <a
                      key={index}
                      href={item.url}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="block bg-gray-700/30 rounded-xl p-4 hover:bg-gray-700/50 transition-all duration-200 border border-gray-600/30 hover:border-green-500/30"
                    >
                      <h3 className="font-semibold text-sm mb-2 text-white line-clamp-2">{item.title}</h3>
                      <p className="text-xs text-gray-400 line-clamp-2">{item.description || item.summary}</p>
                      <p className="text-xs text-green-400 mt-2 font-medium">{item.source}</p>
                    </a>
                  ))
                ) : (
                  <p className="text-center text-gray-400 py-8">Loading news...</p>
                )}
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}

export default App;
