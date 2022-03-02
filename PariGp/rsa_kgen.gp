/**
 * rsa_kgenc.gp
 *  RSA Key Generation
 *  wrtten by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
\\ allocatemem(10*10^6);

/* a^b mod n */
modexp (a, b, n) = {
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

bit_length(n) = {
	local(bs);
	bs = matsize(binary(n));
	return(bs[2]);
}

rsa_kgen(nb, eb) = {
	local (key pb, qb, p, q, n, e, d, lm, g, tmp);
	key = vector(5);

	pb = qb = nb / 2;
	/* generate p */
	until(bit_length(p) == pb,
		p = nextprime(random(2^pb));
	);
	/* generate q */
	until(bit_length(n) == nb,
		until(bit_length(q) == qb && q != p,
			q = nextprime(random(2^qb));
		);
		n = p * q;
	);

	if (q > p,
		tmp = p;
		p = q;
		p = tmp;
	);

	/* LCM(p-1, q-1) */
	lm = lcm(p - 1, q - 1);
	/* GCD(e, lm) = 1 */
	g = 0;
	while(g != 1,
		e = random(2^eb - 1);
		g = gcd(e, lm);
	);
	/* d = 1/e mod lm  */
	d = Mod(1/e, lm);

	key = [p, q, n, e, d];

	return (key);
}

/* Test */
k = rsa_kgen(1024, 24);

printf("p = %d  (%d bits)\n", k[1], bit_length(k[1]));
printf("q = %d  (%d bits)\n", k[2], bit_length(k[2]));
printf("n = %d  (%d bits)\n", k[3], bit_length(k[3]));
printf("e = %d  (%d bits)\n", k[4], bit_length(k[4]));
printf("d = %d  (%d bits)\n", lift(k[5]), bit_length(lift(k[5])));

m1 = random(2^63);
c = modexp(m1, k[4], k[3]);
print("\nC  = ", lift(c));
m2 = modexp(lift(c), lift(k[5]), k[3]);
print("M  = ", m1);
if (m1 == lift(m2), print("==> OK"), print("==> NG"); );

