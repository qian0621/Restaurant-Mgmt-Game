import java.lang.reflect.*;
import java.util.*;
import java.util.regex.*;

/**
 * Generates the UML class diagram (Graphviz DOT) of the game from the COMPILED classes, so the diagram
 * cannot drift from the code. Attributes and operations come from reflection; the relationships are
 * listed below and each one is checked against a real field or superclass (the tool fails otherwise).
 *
 * Usage: java -cp out:tools-out ClassDiagram [full|model|staff|control] > diagram.dot
 */
public class ClassDiagram {
    record Edge(String kind, String from, String to, String field, String tail, String head) {}

    static final String P = "restaurant.";
    // package -> classes (display order)
    static final Map<String, List<String>> PACKAGES = new LinkedHashMap<>();
    static {
        PACKAGES.put("ui", List.of("ui.ConsoleUI", "ui.ManagerCommand", "ui.ManagerCommand$Kind"));
        PACKAGES.put("control", List.of("control.GameController", "control.TurnPlan", "control.GameConfig", "control.GameResult"));
        PACKAGES.put("staff", List.of("staff.Staff", "staff.ManagedStaff", "staff.Waiter", "staff.Chef", "staff.Host", "staff.Cashier",
                "staff.StaffTask", "staff.SeatCustomerTask", "staff.TakeOrderTask", "staff.ServeOrderTask", "staff.CookOrderTask"));
        PACKAGES.put("model", List.of("model.Restaurant", "model.Customer", "model.RegularCustomer", "model.VIPCustomer", "model.CriticCustomer",
                "model.CustomerType", "model.CustomerState", "model.Order", "model.OrderStatus", "model.MenuItem", "model.ComboMeal",
                "model.Menu", "model.Table", "model.WaitingQueue", "model.HappyHour", "model.ArrivalSchedule"));
    }

    static final List<Edge> EDGES = List.of(
        // generalisation (field = null, checked via getSuperclass)
        new Edge("extends", "model.RegularCustomer", "model.Customer", null, "", ""),
        new Edge("extends", "model.VIPCustomer", "model.Customer", null, "", ""),
        new Edge("extends", "model.CriticCustomer", "model.Customer", null, "", ""),
        new Edge("extends", "model.ComboMeal", "model.MenuItem", null, "", ""),
        new Edge("extends", "staff.ManagedStaff", "staff.Staff", null, "", ""),
        new Edge("extends", "staff.Cashier", "staff.Staff", null, "", ""),
        new Edge("extends", "staff.Waiter", "staff.ManagedStaff", null, "", ""),
        new Edge("extends", "staff.Chef", "staff.ManagedStaff", null, "", ""),
        new Edge("extends", "staff.Host", "staff.ManagedStaff", null, "", ""),
        new Edge("extends", "staff.SeatCustomerTask", "staff.StaffTask", null, "", ""),
        new Edge("extends", "staff.TakeOrderTask", "staff.StaffTask", null, "", ""),
        new Edge("extends", "staff.ServeOrderTask", "staff.StaffTask", null, "", ""),
        new Edge("extends", "staff.CookOrderTask", "staff.StaffTask", null, "", ""),
        // composition: the whole creates and owns the parts
        new Edge("comp", "model.Restaurant", "model.Table", "tables", "1", "1..*"),
        new Edge("comp", "model.Restaurant", "model.WaitingQueue", "queue", "1", "1"),
        new Edge("comp", "model.Restaurant", "model.HappyHour", "happyHour", "1", "1"),
        new Edge("comp", "model.Restaurant", "model.Customer", "customers", "1", "0..*"),
        new Edge("comp", "control.GameController", "model.Restaurant", "restaurant", "1", "1"),
        new Edge("comp", "control.GameController", "staff.ManagedStaff", "roster", "1", "3"),
        new Edge("comp", "control.GameController", "staff.Cashier", "cashier", "1", "1"),
        new Edge("comp", "control.TurnPlan", "staff.StaffTask", "tasks", "1", "0..3"),
        new Edge("comp", "control.GameConfig", "model.ArrivalSchedule", "schedule", "1", "1"),
        // aggregation: parts that exist independently of the whole
        new Edge("agg", "model.Restaurant", "model.Menu", "menu", "1", "1"),
        new Edge("agg", "model.Menu", "model.MenuItem", "offerings", "1", "1..*"),
        new Edge("agg", "model.ComboMeal", "model.MenuItem", "first", "1", "1"),
        new Edge("agg", "model.ComboMeal", "model.MenuItem", "second", "1", "1"),
        // plain associations
        new Edge("assoc", "control.GameController", "ui.ConsoleUI", "ui", "1", "1"),
        new Edge("assoc", "control.GameController", "control.GameConfig", "config", "1", "1"),
        new Edge("assoc", "model.Customer", "model.Order", "order", "1", "0..1"),
        new Edge("assoc", "model.Customer", "model.Table", "table", "0..1", "0..1"),
        new Edge("assoc", "model.Customer", "model.CustomerState", "state", "*", "1"),
        new Edge("assoc", "model.Order", "model.Customer", "customer", "0..1", "1"),
        new Edge("assoc", "model.Order", "model.MenuItem", "item", "*", "1"),
        new Edge("assoc", "model.Order", "model.OrderStatus", "status", "*", "1"),
        new Edge("assoc", "model.Table", "model.Customer", "occupant", "0..1", "0..1"),
        new Edge("assoc", "model.WaitingQueue", "model.Customer", "waiting", "0..1", "0..*"),
        new Edge("assoc", "staff.StaffTask", "model.Customer", "target", "*", "1"),
        new Edge("assoc", "ui.ManagerCommand", "ui.ManagerCommand$Kind", "kind", "*", "1")
    );

    // dependencies (dashed arrows) - documented uses without a field; checked only for class existence
    static final List<String[]> DEPS = List.of(
        new String[]{"restaurant.Main", "control.GameController", "creates/runs"},
        new String[]{"control.GameController", "staff.StaffTask", "validates, executes"},
        new String[]{"control.GameController", "ui.ManagerCommand", "reads"},
        new String[]{"control.GameConfig", "control.GameResult", "creates"},
        new String[]{"ui.ConsoleUI", "ui.ManagerCommand", "creates"},
        new String[]{"ui.ConsoleUI", "model.Restaurant", "displays"},
        new String[]{"staff.ManagedStaff", "staff.StaffTask", "creates"},
        new String[]{"staff.StaffTask", "model.Restaurant", "acts on"},
        new String[]{"staff.StaffTask", "control.TurnPlan", "validates against"},
        new String[]{"model.CustomerType", "model.Customer", "creates"},
        new String[]{"model.ArrivalSchedule", "model.CustomerType", "maps turn to"},
        new String[]{"model.Restaurant", "model.Order", "creates"},
        new String[]{"staff.Cashier", "model.Restaurant", "collects payment from"}
    );

    static Class<?> load(String n) throws Exception {
        return Class.forName(n.startsWith("restaurant.") ? n : P + n);
    }

    static String simple(String t) {
        t = t.replaceAll("\\b(?:[a-z_][a-z0-9_]*\\.)+", "");
        return t.replace('$', '.').replace("java.util.", "");
    }

    static String esc(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    static char vis(int m) {
        if (Modifier.isPublic(m)) return '+';
        if (Modifier.isProtected(m)) return '#';
        if (Modifier.isPrivate(m)) return '-';
        return '~';
    }

    static boolean trivial(Method m) {
        String n = m.getName();
        return (n.startsWith("get") || n.startsWith("is")) && m.getParameterCount() == 0;
    }

    static String node(String name, boolean compact) throws Exception {
        Class<?> c = load(name);
        boolean isEnum = c.isEnum(), isRecord = c.isRecord();
        boolean isAbstract = Modifier.isAbstract(c.getModifiers()) && !isEnum;
        String title = esc(simple(c.getName()));
        if (isAbstract) title = "<I>" + title + "</I>";
        String stereo = isEnum ? "&laquo;enumeration&raquo;" : isRecord ? "&laquo;record&raquo;" : "";
        StringBuilder sb = new StringBuilder();
        sb.append("  \"").append(name).append("\" [shape=plain, label=<\n    <TABLE BORDER=\"1\" CELLBORDER=\"0\" CELLSPACING=\"0\" CELLPADDING=\"2\" BGCOLOR=\"white\">\n");
        sb.append("      <TR><TD ALIGN=\"CENTER\" BGCOLOR=\"#e8eef7\">").append(stereo.isEmpty() ? "" : "<FONT POINT-SIZE=\"8\">" + stereo + "</FONT><BR/>")
          .append("<B>").append(title).append("</B></TD></TR>\n");
        // attributes
        List<String> attrs = new ArrayList<>();
        if (isEnum) {
            for (Object k : c.getEnumConstants()) attrs.add(esc(k.toString()));
        } else {
            if (isRecord) {
                for (RecordComponent rc : c.getRecordComponents())
                    attrs.add("- " + esc(rc.getName() + ": " + simple(rc.getGenericType().getTypeName())));
            } else {
                for (Field f : c.getDeclaredFields()) {
                    if (f.isSynthetic()) continue;
                    String line = vis(f.getModifiers()) + " " + f.getName() + ": " + simple(f.getGenericType().getTypeName());
                    if (Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers())) line = "<U>" + esc(line) + "</U>";
                    else line = esc(line);
                    attrs.add(line);
                }
            }
        }
        sb.append("      <TR><TD ALIGN=\"LEFT\" BALIGN=\"LEFT\" BORDER=\"1\" SIDES=\"T\">");
        sb.append(attrs.isEmpty() ? " " : String.join("<BR/>", attrs)).append("</TD></TR>\n");
        // operations
        List<String> ops = new ArrayList<>();
        for (Method m : c.getDeclaredMethods()) {
            if (m.isSynthetic() || m.isBridge() || m.getName().startsWith("lambda$")) continue;
            if (isEnum && (m.getName().equals("values") || m.getName().equals("valueOf") || m.getName().startsWith("$"))) continue;
            if (isRecord && (m.getName().equals("equals") || m.getName().equals("hashCode") || m.getName().equals("toString")
                    || c.getRecordComponents().length > 0 && Arrays.stream(c.getRecordComponents()).anyMatch(r -> r.getName().equals(m.getName())))) continue;
            if (m.getName().equals("toString")) continue;
            if (trivial(m) && !Modifier.isAbstract(m.getModifiers())) continue;   // plain getters are omitted
            StringBuilder p = new StringBuilder();
            Parameter[] ps = m.getParameters();
            for (int i = 0; i < ps.length; i++) {
                if (i > 0) p.append(", ");
                p.append(ps[i].getName()).append(": ").append(simple(ps[i].getParameterizedType().getTypeName()));
            }
            String line = vis(m.getModifiers()) + " " + m.getName() + "(" + p + ")";
            if (m.getReturnType() != void.class) line += ": " + simple(m.getGenericReturnType().getTypeName());
            line = esc(line);
            if (Modifier.isAbstract(m.getModifiers())) line = "<I>" + line + "</I>";
            if (Modifier.isStatic(m.getModifiers())) line = "<U>" + line + "</U>";
            ops.add(line);
        }
        // public constructors are relevant for creation; list them last
        for (Constructor<?> k : c.getDeclaredConstructors()) {
            if (k.isSynthetic() || isEnum || isRecord) continue;
            StringBuilder p = new StringBuilder();
            Parameter[] ps = k.getParameters();
            for (int i = 0; i < ps.length; i++) { if (i > 0) p.append(", "); p.append(ps[i].getName()).append(": ").append(simple(ps[i].getParameterizedType().getTypeName())); }
            if (ps.length == 0 && !Modifier.isPublic(k.getModifiers())) continue;
            if (compact && ps.length > 3) continue;
            ops.add(esc(vis(k.getModifiers()) + " " + simple(c.getName()) + "(" + p + ")"));
        }
        sb.append("      <TR><TD ALIGN=\"LEFT\" BALIGN=\"LEFT\" BORDER=\"1\" SIDES=\"T\">");
        sb.append(ops.isEmpty() ? " " : String.join("<BR/>", ops)).append("</TD></TR>\n    </TABLE>>];\n");
        return sb.toString();
    }

    static void validate(Edge e) throws Exception {
        Class<?> from = load(e.from()), to = load(e.to());
        if (e.kind().equals("extends")) {
            if (from.getSuperclass() != to) throw new IllegalStateException("not a subclass: " + e);
            return;
        }
        Field f;
        try { f = from.getDeclaredField(e.field()); } catch (NoSuchFieldException ex) { throw new IllegalStateException("no field for edge " + e); }
        String t = f.getGenericType().getTypeName();
        if (!t.contains(to.getName()) && !t.contains(to.getName().replace('$', '.'))) throw new IllegalStateException("field type mismatch for " + e + " (" + t + ")");
    }

    public static void main(String[] args) throws Exception {
        String view = args.length > 0 ? args[0] : "full";
        Set<String> keep = new LinkedHashSet<>();
        for (var en : PACKAGES.entrySet()) {
            if (view.equals("full") || view.equals(en.getKey()) || (view.equals("control") && en.getKey().equals("ui"))) keep.addAll(en.getValue());
        }
        if (view.equals("control")) { keep.add("model.Restaurant"); keep.add("staff.ManagedStaff"); keep.add("staff.StaffTask"); keep.add("staff.Cashier"); keep.add("model.ArrivalSchedule"); }
        if (view.equals("staff")) { keep.add("model.Restaurant"); keep.add("model.Customer"); keep.add("control.TurnPlan"); }
        if (view.equals("model")) { keep.remove("model.Restaurant"); keep.add("model.Restaurant"); }

        for (Edge e : EDGES) validate(e);
        for (String[] d : DEPS) { load(d[0]); load(d[1]); }

        boolean compact = view.equals("full");
        StringBuilder out = new StringBuilder();
        out.append("digraph G {\n  rankdir=").append(view.equals("full") ? "TB" : "TB").append(";\n  fontname=\"Helvetica\"; compound=true;\n");
        out.append("  node [fontname=\"Helvetica\", fontsize=").append(compact ? "9" : "10").append("];\n  edge [fontname=\"Helvetica\", fontsize=8];\n");
        out.append("  nodesep=0.35; ranksep=0.55; splines=true;\n");
        for (var en : PACKAGES.entrySet()) {
            List<String> inPkg = new ArrayList<>();
            for (String n : en.getValue()) if (keep.contains(n)) inPkg.add(n);
            for (String n : keep) if (n.startsWith(en.getKey() + ".") && !inPkg.contains(n)) inPkg.add(n);
            if (inPkg.isEmpty()) continue;
            out.append("  subgraph cluster_").append(en.getKey()).append(" {\n    label=<&laquo;package&raquo; restaurant.").append(en.getKey())
               .append(">; style=\"rounded,dashed\"; color=\"#8a94a6\"; fontsize=11;\n");
            for (String n : inPkg) out.append(node(n, compact));
            out.append("  }\n");
        }
        if (keep.contains("restaurant.Main") || view.equals("full")) {
            out.append("  \"restaurant.Main\" [shape=plain, label=<<TABLE BORDER=\"1\" CELLBORDER=\"0\" CELLSPACING=\"0\" CELLPADDING=\"2\"><TR><TD BGCOLOR=\"#e8eef7\"><B>Main</B></TD></TR><TR><TD ALIGN=\"LEFT\" BORDER=\"1\" SIDES=\"T\">+ main(args: String[]) {static}</TD></TR></TABLE>>];\n");
        }
        for (Edge e : EDGES) {
            if (!keep.contains(e.from()) || !keep.contains(e.to())) continue;
            String attrs;
            switch (e.kind()) {
                case "extends" -> attrs = "arrowhead=empty, color=\"#222222\"";
                case "comp" -> attrs = "arrowtail=diamond, dir=back, color=\"#222222\"";
                case "agg" -> attrs = "arrowtail=odiamond, dir=back, color=\"#222222\"";
                default -> attrs = "arrowhead=vee, color=\"#222222\"";
            }
            String ml = e.kind().equals("extends") ? "" :
                ", label=\"" + e.field() + "\", taillabel=\"" + e.tail() + "\", headlabel=\"" + e.head() + "\", labeldistance=1.4";
            // 'from' is the whole/owner: draw so the diamond sits at 'from'
            out.append("  \"").append(e.from()).append("\" -> \"").append(e.to()).append("\" [").append(attrs).append(ml).append("];\n");
        }
        if (!view.equals("model") || true) for (String[] d : DEPS) {
            String f = d[0], t = d[1];
            if (!(keep.contains(f) || f.equals("restaurant.Main") && view.equals("full")) || !keep.contains(t)) continue;
            out.append("  \"").append(f).append("\" -> \"").append(t).append("\" [style=dashed, arrowhead=vee, color=\"#555555\", fontcolor=\"#555555\", label=\"«").append(d[2]).append("»\"];\n");
        }
        // annotations required by the brief
        if (view.equals("full") || view.equals("model")) {
            out.append("  note_enc [shape=note, style=filled, fillcolor=\"#fff7d6\", fontsize=9, label=\"ENCAPSULATION\\lCustomer keeps satisfaction, state, table and order private.\\lIts transitions (seatAt, placeOrder, startEating, pay, abandon)\\lare package-private and only called by Restaurant, so no\\lcaller can skip a step of the dining flow.\\l\"];\n");
            out.append("  note_enc -> \"model.Customer\" [style=dotted, arrowhead=none];\n");
            out.append("  note_poly [shape=note, style=filled, fillcolor=\"#fff7d6\", fontsize=9, label=\"POLYMORPHISM (customers)\\lgetLossPerWaitingTurn(), billFor(), chooseItem() and\\lsatisfactionAtPayment() are overridden by VIP and Critic;\\lGameController and Restaurant call them without type checks.\\l\"];\n");
            out.append("  note_poly -> \"model.CriticCustomer\" [style=dotted, arrowhead=none];\n");
            out.append("  note_poly -> \"model.VIPCustomer\" [style=dotted, arrowhead=none];\n");
            out.append("  note_comp [shape=note, style=filled, fillcolor=\"#fff7d6\", fontsize=9, label=\"COMPOSITION\\lRestaurant creates and owns its Tables, WaitingQueue,\\lCustomers and HappyHour; they live and die with it.\\lComboMeal is built from two existing MenuItems.\\l\"];\n");
            out.append("  note_comp -> \"model.Restaurant\" [style=dotted, arrowhead=none];\n");
        }
        if (view.equals("full") || view.equals("staff")) {
            out.append("  note_poly2 [shape=note, style=filled, fillcolor=\"#fff7d6\", fontsize=9, label=\"POLYMORPHISM (tasks)\\lvalidate() and execute() differ per StaffTask subclass;\\lthe controller treats every task alike (Command pattern).\\l\"];\n");
            out.append("  note_poly2 -> \"staff.StaffTask\" [style=dotted, arrowhead=none];\n");
        }
        out.append("}\n");
        System.out.print(out);
    }
}
