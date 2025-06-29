package com.project01;

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
    
//Hàm hiển thị chi tiết lương
    public String xemChiTiet() {
        String chiTiet = "";
        chiTiet += "=== CHI TIẾT LƯƠNG ===\n";
        chiTiet += "Nhân viên: " + hoTen + "\n";
        chiTiet += "Vị trí: " + viTri + "\n";
       // chiTiet += "Cấp bậc: " + capBac.toUpperCase() + "\n";
        chiTiet += "Lương cơ bản: " + String.format("%.0f", luongCoBan) + "\n";
        chiTiet += "Hệ số vị trí: " + String.format("%.1f", heSoViTri) + "\n";




        if (capBac.equals("manager")) {  //level
            chiTiet += "Cấp bậc: " + capBac.toUpperCase() + "\n";
            chiTiet += "Hệ số cấp bậc manager: " + String.format("%.1f", heSoCapBac) + "\n";
        }else if (viTri.equals("Manager")) {
            chiTiet += "Kinh nghiệm : " + soNamKinhNghiem + " năm \n";
        }
        else {
            chiTiet += "Kinh nghiệm: " + soNamKinhNghiem + " năm (" + tenKinhNghiem +
                      " - x" + String.format("%.1f", heSoKinhNghiem) + ")\n";
            chiTiet += "Cấp bậc: " + capBac.toUpperCase() + "\n";
            chiTiet += "Hệ số cấp bậc employee: " + String.format("%.1f", heSoCapBac) + "\n";
        }

        // Hiển thị thưởng nếu position là Manager
        if (thuongThem > 0) {
            chiTiet += "Thưởng position Manager: " + String.format("%.0f", thuongThem) + "\n";
        }

        chiTiet += "TỔNG LƯƠNG: " + String.format("%.0f", luongCuoiCung) + "\n";
        return chiTiet;
    }
    

    public String xemNganGon() {
        return hoTen + " (" + viTri + "): " + String.format("%.0f", luongCuoiCung);
    }
} 