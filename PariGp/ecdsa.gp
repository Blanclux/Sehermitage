/**
 * ecdsa.gp
 * ECDSA
 *  wrtten by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */

int2ff(n, g) = { subst(Pol(digits(n, g.p)), 'x, g) };
ff2int(x) = { subst(x.pol, variable(x.pol), x.p) };

\\ EC sect283k1
f = ffgen((t^283 + t^12 + t^7 + t^5 + 1) * Mod(1, 2), 't);
E = ellinit([1,0,0,0,1], f);
G_x = 9737095673315832344313391497449387731784428326114441977662399932694280557468376967222;
G_y = 3497201781826516614681192670485202061196189998012192335594744939847890291586353668697;
G = [int2ff(G_x, f), int2ff(G_y, f)];
n = ellorder(E, G);
print("n = ", n);
print("G = ", lift(G));
print("h = ", E.no/n);

\\ Key generation
x = random(n);
P = ellmul(E, G, x);
print("x = ", x);
print("P = ", P);
print("\n");

\\ Message (Hash value)
z = random(n);
print("message : ", z);

\\ Sign
k = random(2^160);
kG = ellmul(E, G, k);
r = Mod(ff2int(kG[1]), n);
s = Mod(k, n)^-1 * (z + r * x);
sig = lift([r, s]);
print("sig : ", sig);

\\ Verify
w = s^-1;
u1 = z * w;
u2 = r * w;
r2 = ff2int(elladd(E, ellmul(E, G, lift(u1)), ellmul(E, P, lift(u2)))[1]);

if (r2 == r, print("Verify OK"), print("Verify NG"); );
