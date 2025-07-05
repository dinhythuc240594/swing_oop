package com.project01;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SalaryCalculator {
    
    // Các số cố định để tính lương
    private static final double LUONG_CO_BAN = 1000.0;           // Lương cơ bản
    private static final double HE_SO_MANAGER_LEVEL = 2.8;       // Hệ số thêm nếu level là manager
    private static final double THUONG_MANAGER_POSITION = 500.0; // Thưởng cho position Manager
    

    //Hàm lấy hệ số theo vị trí công việc
    public static double layHeSoViTri(String viTri) {
        if (viTri.equals("Developer")) {
            return 1.5;
        } else if (viTri.equals("Designer")) {
            return 1.4;
        } else if (viTri.equals("QA")) {
            return 1.3;
        } else if (viTri.equals("QC")) {
            return 1.3;
        } else if (viTri.equals("Manager")) {
            return 3.5;
        } else {
            return 1.0;
        }
    }
    

    //Hàm lấy hệ số kinh nghiệm
    private static double layHeSoKinhNghiem(int soNamKinhNghiem) {
        if (soNamKinhNghiem >= 8) {
            return 1.8;//Leader
        } else if (soNamKinhNghiem >= 5) {
            return 1.5;//senior
        } else if (soNamKinhNghiem >= 2) {
            return 1.2;//middle
        } else {
            return 1.0; //Junior
        }
    }


    //Hàm lấy tên level kinh nghiệm
    private static String layTenKinhNghiem(int soNamKinhNghiem) {
        if (soNamKinhNghiem >= 8) {
            return "Leader";
        } else if (soNamKinhNghiem >= 5) {
            return "Senior";
        } else if (soNamKinhNghiem >= 2) {
            return "Middle";
        } else {
            return "Junior";
        }
    }
    

    //Hàm tính số năm làm việc từ ngày vào công ty
    private static int tinhSoNamLamViec(String ngayVaoLam) {
        if (ngayVaoLam == null || ngayVaoLam.trim().isEmpty()) {
            return 0;
        }
        
        try {
            String[] phanTachNgay = ngayVaoLam.split("-");
            if (phanTachNgay.length > 0) {
                int namVaoLam = Integer.parseInt(phanTachNgay[0]);
                int namHienTai = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
                return namHienTai - namVaoLam;
            }
        } catch (Exception loi) {
            System.out.println("Lỗi khi tính năm làm việc: " + loi.getMessage());
        }
        
        return 0;
    }
    


    // Hàm chính để tính lương nhân viên
    public static ThongTinLuong tinhLuong(int maNhanVien, Connection ketNoiDB) {
        ThongTinLuong thongTin = new ThongTinLuong();
        
        try {
            String cauLenhSQL = "SELECT e.first_name, e.last_name, e.position, e.salary, e.hire_date, u.role " +
                               "FROM employees e " +
                               "INNER JOIN users u ON e.user_id = u.id " +
                               "WHERE e.id = ?";
            
            PreparedStatement pstmt = ketNoiDB.prepareStatement(cauLenhSQL);
            pstmt.setInt(1, maNhanVien);
            ResultSet ketQua = pstmt.executeQuery();
            
            if (ketQua.next()) {
                String hoTen = ketQua.getString("first_name") + " " + ketQua.getString("last_name");
                String viTri = ketQua.getString("position");
                String capBac = ketQua.getString("role");
                double luongCoBan = ketQua.getDouble("salary");
                String ngayVaoLam = ketQua.getString("hire_date");
                
                int soNamKinhNghiem = tinhSoNamLamViec(ngayVaoLam);
                double heSoViTri = layHeSoViTri(viTri);
                double heSoCapBac = capBac.equals("manager") ? HE_SO_MANAGER_LEVEL : 1.0;
                double heSoKinhNghiem = capBac.equals("manager") ? 1.0 : layHeSoKinhNghiem(soNamKinhNghiem);
                String tenKinhNghiem = layTenKinhNghiem(soNamKinhNghiem);
                
                double luongCuoiCung;
                double thuongThem = 0;
                
                if (capBac.equals("manager")) {
                    luongCuoiCung = luongCoBan * heSoViTri * heSoCapBac;
                } else {
                    luongCuoiCung = luongCoBan * heSoViTri * heSoKinhNghiem;
                }
                
                if (viTri.equals("Manager")) {
                    thuongThem = THUONG_MANAGER_POSITION;
                    luongCuoiCung += thuongThem;
                }
                
                thongTin.maNhanVien = maNhanVien;
                thongTin.hoTen = hoTen;
                thongTin.viTri = viTri;
                thongTin.capBac = capBac;
                thongTin.luongCoBan = luongCoBan;
                thongTin.heSoViTri = heSoViTri;
                thongTin.heSoCapBac = heSoCapBac;
                thongTin.soNamKinhNghiem = soNamKinhNghiem;
                thongTin.tenKinhNghiem = tenKinhNghiem;
                thongTin.heSoKinhNghiem = heSoKinhNghiem;
                thongTin.thuongThem = thuongThem;
                thongTin.luongCuoiCung = luongCuoiCung;
                
                System.out.println("Đã tính lương cho " + hoTen + ": " + luongCuoiCung);
            }
            
            ketQua.close();
            pstmt.close();
            
        } catch (SQLException loi) {
            System.out.println("Lỗi khi tính lương: " + loi.getMessage());
        }
        
        return thongTin;
    }
}