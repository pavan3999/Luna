package studio.orchard.luna.Component.Resolver;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import studio.orchard.luna.Component.IO;
import studio.orchard.luna.Component.SerializedClass.v0.BookShelf;

public class ContentResolver {
    private Context context;
    private BookShelf.Book book;
    private int chapterIndex;
    private List<String> text;
    private List<String> processedText;
    private List<Bitmap> illustrations;
    private float contentWidth;
    private int totalLineCount;
    private int pageLineCount;
    private int pageCount;
    private int textPageCount;
    private int illustrationPageCount;
    private boolean isResolved;

    public ContentResolver(Context context, BookShelf.Book book, int chapterIndex){
        this.context = context;
        this.book = book;
        this.chapterIndex = chapterIndex;
        this.processedText = new ArrayList<>();
        this.illustrations = new ArrayList<>();
        readTextFile();
    }

    private void readTextFile(){
        String path = "/" + book.bookSeriesID + "/" + book.index + "/" + chapterIndex + "/" + chapterIndex + ".txt";
        BufferedReader bufferedReader = IO.fileToStreamBuffer(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + path);
        text = new ArrayList<>();
        try {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                text.add(line); }
            bufferedReader.close();
        }catch (IOException ignored){ }
    }

   public void resolve(TextView textView){
       resolveText(textView);
       resolveIllustration();
       pageCount = textPageCount + illustrationPageCount;
       isResolved = true;
   }

   private void resolveIllustration(){
       if(illustrationPageCount > 0) return;
       for(int i = 0; i < book.illustrationNum.size(); i++){
           String path = "/" + book.bookSeriesID + "/" + book.index + "/" + chapterIndex + "/" + "Illustration" + "/" + i + ".jpg";
           File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), path);
           if(!file.exists()) continue;
           illustrations.add(IO.fileToBitmap(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + path));
       }
       illustrationPageCount = illustrations.size();
   }

    public void resolveText(TextView textView){
        if(isResolved || textView.getHeight() == 0) return;
        processedText.clear();
        totalLineCount = 0;
        pageLineCount = (textView.getHeight() - 70) / textView.getLineHeight();
        for(String textLine : text){
            char[] chars = textLine.toCharArray();
            StringBuilder line = new StringBuilder();
            float lineWidth = 0;
            for(int i = 0; i < chars.length; i++) {
                float charWidth = textView.getPaint().measureText(chars, i, 1);
                if(lineWidth + charWidth <= textView.getWidth()) {
                    line.append(chars[i]);
                    lineWidth += charWidth;
                }else if(i > 0 && isPunctuation(chars[i]) && !isPunctuation(chars[i - 1])) {
                    String lastChar = String.valueOf(chars[i - 1]);
                    line.delete(line.length() - 1, line.length());
                    line.append("\n");
                    totalLineCount++;
                    processedText.add(line.toString());
                    line.delete(0, line.length());
                    line.append(lastChar).append(chars[i]);
                    lineWidth = textView.getPaint().measureText(lastChar) + charWidth;
                }else{
                    line.append("\n");
                    totalLineCount++;
                    processedText.add(line.toString());
                    line.delete(0, line.length());
                    line.append(chars[i]);
                    contentWidth = Math.max(lineWidth, contentWidth);
                    lineWidth = charWidth;
                }
            }
            line.append("\n");
            totalLineCount++;
            processedText.add(line.toString());
            line.delete(0, line.length());
        }
        textPageCount = (totalLineCount / pageLineCount) + 1;
    }

    private boolean isPunctuation(char c){
        return (c == ',' || c == '，' || c == '.' || //处理特殊符号，防止其出现在句首
                c == '。' || c == '!' || c == '！' ||
                c == ':' || c == '：' || c == ';' ||
                c == '；' || c == ')' || c == '）' ||
                c == '>' || c == '》' || c == ']' ||
                c == '】' || c == '}' || c == '”' ||
                c == '%' || c == '~' || c == '」' || c == '』' ||
                c == '、' || c == '?' || c == '？'|| c == '〉');
    }


    public String getText(int pageNum){
        pageNum -= illustrationPageCount;
        if(totalLineCount == 0 || pageLineCount == 0) return "";
        int restLine = totalLineCount % pageLineCount;
        StringBuilder output = new StringBuilder();
        if (pageNum < textPageCount) {
            if(pageNum < 1) pageNum = 1;
            for (int i = (pageNum - 1) * pageLineCount; i < pageNum * pageLineCount; i++) {
                output.append(processedText.get(i));
            }
        } else if (pageNum == textPageCount) {
            for (int i = (pageNum - 1) * pageLineCount; i < (pageNum - 1) * pageLineCount + restLine; i++) {
                output.append(processedText.get(i));
            }
        }
        return output.toString();
    }

    public Bitmap getIllustration(int pageNum) {
        return pageNum > illustrations.size() ? null : illustrations.get(pageNum - 1);
    }

    public boolean isResolved() { return isResolved; }
    public void clear() {
        isResolved = false;
        totalLineCount = 0;
        pageLineCount = 0;
        textPageCount = 0;
        pageCount = illustrationPageCount;
        contentWidth = 0;
        processedText.clear();
    }

    public int getPageLineCount(){ return pageLineCount; }
    public int getTotalLineCount(){ return totalLineCount; }
    public int getPageCount(){ return pageCount; }
    public int getIllustrationPageCount(){ return illustrationPageCount; }
    public int getTextPageCount(){ return textPageCount; }
    public float getContentTextWidth(){ return contentWidth; }
}
