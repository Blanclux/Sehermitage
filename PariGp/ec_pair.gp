/**
 * ec_pair.gp
 * Tate Pairing Test
 *  wrtten by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
allocatemem(10*10^6);

tate(E,P,Q,m,q,k) = elltatepairing(E,P,Q,m)^((q^k-1)/m);

curve_print(E) = {
	print("\n< Elliptic curve parameter >");
	print("a4   : ", lift(E.a4));
	print("a6   : ", lift(E.a6));
	print("p    : ", lift(E.p));
	print("#E   : ", E.no);
	print("trace: ", 1 + E.p - E.no);
}

\\ Tate pairing test
pair_test(E1, E2, q, m, k) = {
	r = random(q);
	print("r = ", r);
	h = E1.no / m;
	P = ellmul(E1, random(E1), h);
	print("P = ", P);
	print("[m]P = ", ellmul(E1, P, m));
	Q = random(E2);
	R = random(E2);
	print("Q = ", Q);
	print("R = ", R);

	tp = tate(E2, P, Q, m, q, k);
	print("Tp(P, Q) = ", tp);
	print("\n");
	print("Tp(P, Q)^m = ", tp^m); 
	if (tp^m == 1, print("==> OK"), print("==> NG"););

	tp = tate(E2, P, elladd(E2, Q, R), m, q, k);
	tp1 = tate(E2, P,  Q, m, q, k);
	tp2 = tate(E2, P,  R, m, q, k);
	print("Tp(P, Q+R) = Tp(P, Q)¡¦Tp(P, R)");
	if (tp == tp1 * tp2, print("==> OK"), print("==> NG"););


	tp = tate(E2, P, Q, m, q, k);
	tp1 = tate(E2, P, ellmul(E2, Q, r), m, q, k);
	tp2 = tate(E2, ellmul(E2, P, r), Q, m, q, k);
	print("Tp(P, rQ) = Tp(rP, Q)");
	if (tp1 == tp1, print("==> OK"), print("==> NG"); );
	print("Tp(P, rQ) = Tp(P, Q)^r");
	if (tp1 == tp^r, print("==> OK"), print("==> NG"); );

	P = ellmul(E1, P, m);
	tp = tate(E2, P, Q, m, q, k);
	print("Tp(P=O, Q) = ", tp);
	if (tp == 1, print("==> OK"), print("==> NG"); );
}

print("< Tate Pairing Test >");
print("\n160 bit OD curve");
q = 625852803282871856053922297323874661378036491717;
a = 625852803282871856053922297323874661378036491714;
b = 423976005090848776334332509669574781621802740510;

E1 = ellinit([0, 0, 0, a, b]*Mod(1, q), q);
hs = factor(E1.no);

h = hs[1,1];

print("h = ", h); 

curve_print(E1);

ff = Mod(1, q) *t^6 + Mod(1, q)*t^1 + Mod(7, q);
g = ffgen(ff);
k = poldegree(ff);
E2 = ellinit([0, 0, 0, a, b]*Mod(1, q), g);
curve_print(E2);

print("f = ", g.mod);

print("k = ", k);
m = E1.no / h;
print("m = ", m);

pair_test(E1, E2, q, m, k);
