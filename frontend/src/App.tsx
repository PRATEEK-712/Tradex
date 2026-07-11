import { useEffect, useMemo, useState } from 'react';
import type { User } from 'oidc-client-ts';
import { authEnabled, getCurrentUser, userManager } from './auth';
import { fetchModule, ModuleKey } from './api';

type RecordItem = Record<string, string | number | boolean | null>;

const modules: Array<{ key: ModuleKey; label: string; icon: string; accent: string }> = [
  { key: 'workflows', label: 'Workflows', icon: 'CL', accent: '#2563eb' },
  { key: 'inventory', label: 'Inventory', icon: 'BX', accent: '#0f766e' },
  { key: 'staff', label: 'Staff', icon: 'US', accent: '#7c3aed' },
  { key: 'orders', label: 'Orders', icon: 'SO', accent: '#c2410c' },
  { key: 'invoices', label: 'Billing', icon: 'IN', accent: '#be123c' },
];

const demoData: Record<ModuleKey, RecordItem[]> = {
  workflows: [
    { id: 1, title: 'Approve dispatch documents', department: 'OPERATIONS', owner: 'Aarav Mehta', status: 'IN_PROGRESS', dueDate: '2026-06-05' },
    { id: 2, title: 'Supplier compliance review', department: 'PROCUREMENT', owner: 'Nisha Rao', status: 'OPEN', dueDate: '2026-06-10' },
  ],
  inventory: [
    { id: 1, sku: 'RM-COP-001', name: 'Copper Cathodes', category: 'Metals', quantity: 840, reorderPoint: 250, warehouse: 'Mumbai Central' },
    { id: 2, sku: 'AG-CAS-044', name: 'Processed Cashew Kernels', category: 'Agri Commodities', quantity: 120, reorderPoint: 180, warehouse: 'Navi Mumbai' },
  ],
  staff: [
    { id: 1, fullName: 'Aarav Mehta', department: 'OPERATIONS', role: 'Operations Lead', active: true },
    { id: 2, fullName: 'Nisha Rao', department: 'BILLING', role: 'Billing Manager', active: true },
  ],
  orders: [
    { id: 1, orderNumber: 'SO-2026-1001', customerName: 'Kaveri Retail Exports', totalAmount: 184500, status: 'IN_PROGRESS' },
  ],
  invoices: [
    { id: 1, invoiceNumber: 'INV-2026-0881', customerName: 'Kaveri Retail Exports', amount: 184500, status: 'OPEN', dueDate: '2026-06-14' },
  ],
};

export function App() {
  const [active, setActive] = useState<ModuleKey>('workflows');
  const [records, setRecords] = useState<Record<ModuleKey, RecordItem[]>>(demoData);
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const activeModule = modules.find((module) => module.key === active)!;
  const totals = useMemo(() => ({
    openTasks: records.workflows.filter((item) => item.status !== 'CLOSED').length,
    stockAlerts: records.inventory.filter((item) => Number(item.quantity) <= Number(item.reorderPoint)).length,
    receivables: records.invoices.reduce((sum, item) => sum + Number(item.amount || 0), 0),
    orderValue: records.orders.reduce((sum, item) => sum + Number(item.totalAmount || 0), 0),
  }), [records]);

  useEffect(() => {
    getCurrentUser().then(setUser).catch((reason) => setError(String(reason)));
  }, []);

  async function refresh() {
    setLoading(true);
    setError(null);
    try {
      const entries = await Promise.all(
        modules.map(async (module) => [module.key, await fetchModule<RecordItem>(module.key, user?.id_token || user?.access_token)] as const),
      );
      setRecords(Object.fromEntries(entries) as Record<ModuleKey, RecordItem[]>);
    } catch (reason) {
      setError(String(reason));
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <IconMark>TR</IconMark>
          <div>
            <strong>TradeOps</strong>
            <span>Company workflow suite</span>
          </div>
        </div>
        <nav className="nav-list" aria-label="Operations modules">
          {modules.map((module) => {
            return (
              <button key={module.key} className={active === module.key ? 'active' : ''} onClick={() => setActive(module.key)}>
                <IconMark>{module.icon}</IconMark>
                <span>{module.label}</span>
              </button>
            );
          })}
        </nav>
        <div className="auth-panel">
          <span>{authEnabled ? user?.profile?.email || user?.profile?.name || 'OIDC session' : 'Demo mode'}</span>
          {authEnabled ? (
            user ? (
              <button onClick={() => userManager?.signoutRedirect()}><IconMark>LO</IconMark> Sign out</button>
            ) : (
              <button onClick={() => userManager?.signinRedirect()}><IconMark>LI</IconMark> Sign in</button>
            )
          ) : null}
        </div>
      </aside>

      <section className="workspace">
        <header className="topbar">
          <div>
            <p>Trading company operations</p>
            <h1>{activeModule.label}</h1>
          </div>
          <button className="primary-action" onClick={refresh} disabled={loading}>
            <IconMark className={loading ? 'spin' : ''}>RF</IconMark>
            Refresh
          </button>
        </header>

        {error ? (
          <div className="notice" role="alert">
            <IconMark>!</IconMark>
            <span>{error}. Showing local demo data.</span>
          </div>
        ) : null}

        <section className="metrics" aria-label="Operational metrics">
          <Metric label="Open workflows" value={totals.openTasks.toString()} />
          <Metric label="Stock alerts" value={totals.stockAlerts.toString()} />
          <Metric label="Order value" value={formatMoney(totals.orderValue)} />
          <Metric label="Receivables" value={formatMoney(totals.receivables)} />
        </section>

        <section className="data-surface">
          <div className="section-heading" style={{ borderColor: activeModule.accent }}>
            <IconMark>{activeModule.icon}</IconMark>
            <div>
              <h2>{activeModule.label} register</h2>
              <span>{records[active].length} records</span>
            </div>
          </div>
          <DataTable rows={records[active]} />
        </section>
      </section>
    </main>
  );
}

function IconMark({ children, className = '' }: { children: string; className?: string }) {
  return <span className={`icon-mark ${className}`}>{children}</span>;
}

function Metric({ label, value }: { label: string; value: string }) {
  return (
    <article className="metric">
      <span>{label}</span>
      <strong>{value}</strong>
    </article>
  );
}

function DataTable({ rows }: { rows: RecordItem[] }) {
  const columns = Array.from(new Set(rows.flatMap((row) => Object.keys(row)))).slice(0, 7);

  if (rows.length === 0) {
    return <div className="empty-state">No records yet.</div>;
  }

  return (
    <div className="table-wrap">
      <table>
        <thead>
          <tr>
            {columns.map((column) => <th key={column}>{labelize(column)}</th>)}
          </tr>
        </thead>
        <tbody>
          {rows.map((row) => (
            <tr key={String(row.id)}>
              {columns.map((column) => <td key={column}>{formatCell(row[column])}</td>)}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function labelize(value: string) {
  return value.replace(/([A-Z])/g, ' $1').replace(/^./, (char) => char.toUpperCase());
}

function formatCell(value: RecordItem[string]) {
  if (typeof value === 'boolean') {
    return value ? 'Yes' : 'No';
  }
  if (typeof value === 'number' && value > 1000) {
    return formatMoney(value);
  }
  return value ?? '-';
}

function formatMoney(value: number) {
  return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(value);
}
