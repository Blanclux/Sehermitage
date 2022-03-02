/**
 *  chkPassword
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */

/**
 * Determines the strength of a given password
 *
 * @param string arg_password  The password
 * @param string arg_spc_chars A string of special characters
 * @return string The verdict as 'Very Weak'|'Weak'|'Medium'|'Strong'|'Very Strong'
 */
function Password(arg_password, arg_spc_chars)
{
	var password = arg_password;
	var spc_chars = arg_spc_chars;
	this.lcase_count = 0;	// lowercase occurrence count
	this.ucase_count = 0;	// uppercase occurrence count
	this.num_count = 0;		// number occurrence count
	this.schar_count = 0;	// special character occurrence count
	this.length = 0;		// length of password string
	this.strength = 0;		// strength value of password
	this.runs_score = 0;	// runs score
	this.verdict = '';		// textual strength indication

	var verdict_conv = {'weak':20, 'medium':40, 'strong':150, 'stronger':500};

	// Weighting factors
	var flc = 1.0;  // lowercase factor
	var fuc = 1.2;  // uppercase factor
	var fnm = 1.3;  // number factor
	var fsc = 1.5;  // special char factor

	this.getStrength = function()
	{
		if ((this.run_score = this.detectRuns()) <= 1) {
			return "Very Weak";
		}

		var regex_sc = new RegExp('['+spc_chars+']', 'g');

		this.lcase_count = password.match(/[a-z]/g);
		this.lcase_count = (this.lcase_count) ? this.lcase_count.length : 0;
		this.ucase_count = password.match(/[A-Z]/g);
		this.ucase_count = (this.ucase_count) ? this.ucase_count.length : 0;
		this.num_count   = password.match(/[0-9]/g);
		this.num_count   = (this.num_count) ? this.num_count.length : 0;
		this.schar_count = password.match(regex_sc);
		this.schar_count = (this.schar_count) ? this.schar_count.length : 0;
		this.length = password.length;

		var avg = this.length / 4;

		// I'm dividing by (avg + 1) to linearize the strength a bit.
		// To get a result that ranges from 0 to 1, divide by Math.pow(avg + 1, 4)
		this.strength = password.length * (1 + password.length / 10) +
						((this.lcase_count * flc + 1) * 
						(this.ucase_count * fuc + 1) *
						(this.num_count * fnm + 1) * 
						(this.schar_count * fsc + 1)) / (avg + 1);

		if (this.strength > verdict_conv.stronger) {
			this.verdict = 'Very Strong';
		}
		else if (this.strength > verdict_conv.strong) {
			this.verdict = 'Strong';
		}
		else if (this.strength > verdict_conv.medium) {
			this.verdict = 'Medium';
		}
		else if (this.strength > verdict_conv.weak) {
			this.verdict = 'Weak';
		}
		else {
			this.verdict = "Very Weak";
		}

		return this.verdict;
    }

	// The difference of adjacent equivalent char values is zero. 
	// The greater the difference, the higher the result.
	//  'aaaaa' sums to 0. 'abcde' sums to 1.  'acegi' sums to 2, etc.
	//  'aaazz', which has a sharp edge, sums to  6.25.
	// Any thing 1 or below is a run, and should be considered weak.
	this.detectRuns = function()
	{
		var parts = password.split('');
		var ords = new Array();
		for (i in parts) {
			ords[i] = parts[i].charCodeAt(0);
		}

		var accum = 0;
		var lasti = ords.length - 1

		for (var i=0; i < lasti; ++i) {
			accum += Math.abs(ords[i] - ords[i+1]);
		}

		return accum / lasti;
	}

	this.toString = function()
	{
		return '小文字 : ' + this.lcase_count
				+ '\n大文字 : '+ this.ucase_count
				+ '\n数字   : '+ this.num_count
				+ '\n記号   : '+ this.schar_count
				+ '\n文字数 : '+ this.length;
	}

	this.getScore = function()
	{
		return this.strength;
	}
}

function chkPassword(passwd)
{
	var special_chars = "~!@#$%&*^_+-.";
	var strVerdict = "Weak";
	var strLog     = "";

	var pw = new Password(passwd, special_chars);

	strVerdict = pw.getStrength();
	if (pw.ucase_count == 0) {
		strLog += "大文字を追加\n";
	}
	if (pw.num_count == 0) {
		strLog += "数字を追加\n";
	}
	if (pw.schar_count == 0) {
		strLog += "記号を追加 : " +  special_chars + "\n";
	}
	if (pw.run_score <= 1) {
		strLog += "連続は避ける (例： 'aaaa', 'abcde', '1234').\n";
	}

	document.forms.passwordForm.score.value = pw.getScore().toFixed(0);
	document.forms.passwordForm.verdict.value = strVerdict;
	if (strLog != "") {
		document.forms.passwordForm.matchlog.value = pw.toString() + "\n\n[ヒント]\n" + strLog;
	}
	else {
		document.forms.passwordForm.matchlog.value = pw.toString();
	}
}
