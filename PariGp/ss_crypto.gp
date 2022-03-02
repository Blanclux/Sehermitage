/**
 * ss_crypto.gp
 *  Secret sharing cryptography
 *  wrtten by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */

/* a^b mod n */
modexp(a, b, n) = {
	local(d, bin);
	d = 1;
	bin = binary(b);
	for (i = 1, length(bin),
		d = Mod(d*d, n);
		if (bin[i] == 1,
			d = Mod(d*a, n);
		);
	);
	return (d);
}

/**
 * Prime generation p (q|p-1)
 */
generate_p(q, n) = {
	local(p);

	p = random(2 << n);
	p = nextprime(p);
	while ((p - 1) % q != 0,
		p = p + 1;
		p = nextprime(p);
	);
	return (p);
}

/**
 * Generate g
 */
generate_g(p, q) = {
	local(g, e, h);

	e = (p - 1)/q;
	for (h = 2, p-2,
		g = modexp(h, e, p);
		if (g == 1, next,
			return (g);
		);
	);
}

/**
 * ElGamal encrypt
 */
elgamal_enc(m, pk, g, p) = {
	local(C, r);
	C = vector(2);

	r =random(p);
	C[1] = modexp(g, r, p);
	C[2] = Mod(m * modexp(pk, r, p), p);

	return (C);
}

/**
 * ElGamal decrypt
 */
elgamal_dec(c1, c2, sk, p) = {
	local(m);

	m = Mod(c2 / (c1^sk), p);
	return (m);
}

/**
 * Lagrange coefficient Li
 */
genLagrange(z, k, q) = {
	local(L, tmp);
	L = vector(k);

	for (i = 1, k,
		L[i] = 1;

		for (j = 1, k,
			if (j == i, next,
				tmp = Mod(z[j], q) - Mod(z[i], q);
				tmp = Mod(1 / tmp, q);
				tmp = Mod(z[j] * tmp, q);
				L[i] = Mod(L[i] * tmp, q);
			);
		);
	);
	return (L);
};

/**
 * Polynomial generation fi
 */
genFuncS(n, k, q) = {
	local(cv, fx);
	cv = vector(k);
	fx = vector(n);

	for (i = 1, n,
		cv[1] = random(q);
		while (cv[1] == 0,
			cv[1] = random(q);
		);
		cv[k] = random(q);
		while (cv[k] == 0,
			cv[k] = random(q);
		);
		for (i = 2, k-1,
			cv[i] = random(q);
		);
		fx[i] = Polrev(cv);
	);
	return (fx);
}


/**
 * Distributed private key fi(j)
 */
genDistribKey(fxx, n) = {
	local(dk);
	dk = matrix(n, n);

	for (i = 1, n,
		for (j = 1, n,
			dk[i, j] = subst(fxx[i], x, j);
		);
	);
	return (dk);
}

/**
 * Public value Gi,j
 */
genPublicVal(fxx, n, g, p) = {
	local(G, e);
	G = matrix(n, n);

	for (i = 1, n,
		for (j = 1, n,
			e = subst(fxx[i], x, j);
			G[i,j] = modexp(g, e, p);
		);
	);

	return (G);
}

/**
 * Public key T
 */
genPublicKey(G, L, z, n, k, p) = {
	local(tmp1, tmp2);

	tmp1 = 1;
	for (i = 1, n,
		for (j = 1, k,
			tmp2 = modexp(G[i,j], lift(L[z[j]]), p);
			tmp1 = Mod(tmp1 * tmp2, p);
		);
	);
	return (tmp1);
}

/**
 * Private key generation g'
 */
genPrivateKey(H, L, z, k, p) = {
	local(tmp1, tmp2);

	tmp1 = 1;
	for (i = 1, k,
		tmp2 = modexp(H[i], lift(L[z[i]]), p);
		tmp1 = Mod(tmp1 * tmp2, p);
	);
	return (tmp1);
}

/**
 * Distributed decrypt information
 *  Hi = (g^r)¦²{j=1¡ÁN} fj(i) (mod p) (j = 1,...,N)
 */
distDecrypt(C1, dx, n, p) = {
	local(H, e);

	H = vector(n);
	e = vector(n);
	for (i = 1, n,
		e[i] = 0;
		for (j = 1, n,
			e[i] = Mod(dk[j,i] + e[i], p);

		);
		H[i] = modexp(C1, lift(e[i]), p);
	);
	return (H);
}

checkT(fxx, g, n, p) = {
	local(a);

	a = 0;
	for (i = 1, n,
		a = a + subst(fxx[i], x, 0);
	);
	a = Mod(g^a, p);
	return (a);
}

/**
 * Main program
 */
/* allocatemem(5*9^10); */
n = 7;
k = 5;
pb = 64;
qb = 16;

q = random(2 << qb);
q = nextprime(q);
p = generate_p(q, pb);
g = generate_g(p, q);

printf("N = %d, K = %d", n, k);
print("p = ", p);
print("q = ", q);
print("g = ", g);
print("g^q (mod p) = ", lift(Mod(g^q, p)));

/* Polynomial */
fxx = genFuncS(n, k, q);
for (i = 1, n, print("fx = ", fxx[i]));

/* Distributed keys */
dk = genDistribKey(fxx, n);
print("Distribute key : ", dk);

/* Public value Gi,j */
G = genPublicVal(fxx, n, g, p);
print("Gi,j : ", lift(G));

/* Public key T */
z = [1, 2, 3, 4, 5];
L = genLagrange(z, k, q);
T = genPublicKey(G, L, z, n, k, p);
print("Public Key : ", lift(T));
\\T2 = checkT(fxx, g, n, p);
\\print("Public Key : ", lift(T2));

/* Encrypt */
m1 = random(p);
print("Plain text : ", m1);
C = elgamal_enc(m1, T, g, p);
print("Encrypt C  : ", lift(C));

/* Distributed dcrypt information Hi */
H = distDecrypt(C[1], dx, n, p);
print("Decrypt H  : ", lift(H));

/* Private key information */
g2 = genPrivateKey(H, lift(L), z, k, p);
print("Private key: ", lift(g2));

/* Decrypt */
m2 = Mod(C[2] / g2, p);
print("Decrypt M  : ", lift(m2));

if (m2 == m1, print("==> OK"), print("==> NG"); );
