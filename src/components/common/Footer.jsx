import { Link } from 'react-router-dom'

export default function Footer() {
  return (
    <footer className="bg-gray-900 text-gray-400 py-10 mt-auto">
      <div className="max-w-6xl mx-auto px-4">
        <div className="grid grid-cols-2 md:grid-cols-4 gap-6 mb-8">
          <div className="col-span-2 md:col-span-1">
            <div className="flex items-center gap-2 mb-2">
              <span>🚂</span>
              <span className="text-white font-bold">Rail<span className="text-orange-400">Yatra</span></span>
            </div>
            <p className="text-xs">India's smarter train booking platform — faster, cleaner, better than IRCTC.</p>
          </div>
          <div>
            <h4 className="text-white text-sm font-semibold mb-2">Quick Links</h4>
            {[['/', 'Home'], ['/pnr', 'PNR Status'], ['/my-bookings', 'My Bookings']].map(([to, l]) => (
              <Link key={to} to={to} className="block text-xs py-1 hover:text-white transition">{l}</Link>
            ))}
          </div>
          <div>
            <h4 className="text-white text-sm font-semibold mb-2">Support</h4>
            {['Help Center', 'Refund Policy', 'Contact Us'].map(l => (
              <a key={l} href="#" className="block text-xs py-1 hover:text-white transition">{l}</a>
            ))}
          </div>
          <div>
            <h4 className="text-white text-sm font-semibold mb-2">Legal</h4>
            {['Privacy Policy', 'Terms of Service'].map(l => (
              <a key={l} href="#" className="block text-xs py-1 hover:text-white transition">{l}</a>
            ))}
          </div>
        </div>
        <div className="border-t border-gray-700 pt-4 flex flex-wrap justify-between text-xs gap-2">
          <span>© 2024 RailYatra. Built with ❤️ in India.</span>
          <div className="flex gap-4">
            <span>🔒 Secure</span><span>📱 Mobile Ready</span><span>⚡ Real-time</span>
          </div>
        </div>
      </div>
    </footer>
  )
}
