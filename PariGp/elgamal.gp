/**
 * elgamal.gp
 * EC ElGamal
 *  wrtten by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
\\ System parameter
q = 625852803282871856053922297323874661378036491717;
a = 625852803282871856053922297323874661378036491714;
b = 423976005090848776334332509669574781621802740510;
E = ellinit([0, 0, 0, a, b], q);

G = E.gen[1];
n = ellorder(E, G);
print("q = ", q);
print("n = ", n);
print("G = ", lift(G));

\\ Key generation
x = random(n);
print("x = ", x);
P = ellmul(E, G, x);
print("P = ", P);
print("\n");

\\ Plain Text
\\ M \in E(F_q)
Q = random(E);
m = Q[1];
print("Message : ", lift(m));
M1 = [Mod(m, q), ellordinate(E, m)[1]];

\\ Encrypt
r = random(n);
C1 = ellmul(E, G, r);
C2 = elladd(E, M1, ellmul(E, P, r));
C = [C1, C2];
print("Encrypt : ", lift(C));

\\ Decrypt
M2 = ellsub(E, C2, ellmul(E, C1, x));
print("Decrypt : ", lift(M2[1]));

if (M1 == M2, print("==> OK"), print("==> NG"); );
