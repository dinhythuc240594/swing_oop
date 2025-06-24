package com.project01;


import java.util.Locale;

import java.util.ResourceBundle;

public class ThongTinLuong {
    // Thông tin cơ bản
    public int maNhanVien;
    public String hoTen;
    public String viTri;           // position: Developer, Designer, QA, QC, Manager
    public String capBac;          // level: manager hoặc employee
    public double luongCoBan;
    
    // Các hệ số tính lương
    public double heSoViTri;
    public double heSoCapBac;
    public int soNamKinhNghiem;
    public String tenKinhNghiem;
    public double heSoKinhNghiem;
    public double thuongThem;      // Thưởng 500 cho position Manager
    
    // Kết quả cuối cùng
    public double luongCuoiCung;
    
	private ResourceBundle messages;
	private static final String DEFAULT_LANGUAGE = "English";
	private String currentLanguage = DEFAULT_LANGUAGE;
    
	public void setLanguage(Locale locale) {
		this.messages = ResourceBundle.getBundle("messages", locale);
	}
	
	//Hàm hiển thị chi tiết lương
    public String xemChiTiet() {
        String chiTiet = "";
        chiTiet += "=== " + messages.getString("salary.title") +"===\n";
        chiTiet += messages.getString("salary.employee") + ": " + hoTen + "\n";
        chiTiet += messages.getString("salary.position") + ": " + viTri + "\n";

        chiTiet += messages.getString("salary.basic") + ": " + String.format("%.0f", luongCoBan) + "\n";
        chiTiet += messages.getString("salary.coefficient") + ": " + String.format("%.1f", heSoViTri) + "\n";

        if (capBac.equals("manager")) {  //level
            chiTiet += messages.getString("salary.level") + ": " + capBac.toUpperCase() + "\n";
            chiTiet += messages.getString("salary.manage.level") + ": " + String.format("%.1f", heSoCapBac) + "\n";
        }else if (viTri.equals("Manager")) {
            chiTiet += messages.getString("salary.experience") + ": " + soNamKinhNghiem + " " + messages.getString("salary.year") + "\n";
        }
        else {
            chiTiet += messages.getString("salary.experience") + ": " + soNamKinhNghiem + " " + messages.getString("salary.year") + " (" + tenKinhNghiem +
                      " - x" + String.format("%.1f", heSoKinhNghiem) + ")\n";
            chiTiet += messages.getString("salary.level") + ": " + capBac.toUpperCase() + "\n";
            chiTiet += messages.getString("salary.coefficient.employee") + ": " + String.format("%.1f", heSoCapBac) + "\n";
        }

        // Hiển thị thưởng nếu position là Manager
        if (thuongThem > 0) {
            chiTiet += messages.getString("salary.manage.benefit") + ": " + String.format("%.0f", thuongThem) + "\n";
        }

        chiTiet += messages.getString("salary.sum.salary") + ": " + String.format("%.0f", luongCuoiCung) + "\n";
        return chiTiet;
    }
    
    public String xemNganGon() {
        return hoTen + " (" + viTri + "): " + String.format("%.0f", luongCuoiCung);
    }
	
    
} 